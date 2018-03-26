package at.tuwien.ict.miklas.mind.aconamind.mind;

import java.util.Map;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer.Builder;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.miklas.aconamind.datatransferobjects.sensorDataDTO;

public class AconaTestMind extends CellFunctionThreadImpl {
	
	private static Logger log = LoggerFactory.getLogger(AconaTestMind.class);
	
	private MultiLayerNetwork neuralNet;
	private sensorDataDTO sensorData;
	private INDArray trainingData = Nd4j.zeros(5, 4);
	private INDArray distancesUp = Nd4j.zeros(5, 1);
	private int trainingCounter = 0;
	
	
	public final static String INPUTBUFFER = "inputbuffer";
	public final static String OUTPUTBUFFER = "inputbuffer";
	//public final static String PERCEPTIONADDRESS = INPUTBUFFER + ".perception";
	public final static String SENSORDATAADDRESS = INPUTBUFFER + ".sensordata";
	public final static String SCOREADDRESS = INPUTBUFFER + ".score";
	public final static String ACIONADDRESS = OUTPUTBUFFER + ".action";
	
	private String action = "NONE";

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		
		initNeuralNet();
		this.addManagedDatapoint(DatapointConfig.newConfig(SENSORDATAADDRESS, SENSORDATAADDRESS, SyncMode.SUBSCRIBEONLY));
	}
	
	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest param, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	//static int count = 0;
	@Override
	protected void executeFunction() throws Exception {
		//log.debug("Create random actions");
		//String[] allactions = {"MOVE_FORWARD", "TURN_LEFT", "TURN_RIGHT", "EAT", "ATTACK"};
		//action = allactions[(int) (allactions.length*Math.random())];
		/*if(count > 5)
			action = "TURN_RIGHT";
		count++;*/
		//log.debug("Selected action={}", action);
		
		action = "TURN_RIGHT";
		
		// list off input values, sensor data for 4 input-neurons each
        INDArray inputForNeuralNet = Nd4j.zeros(1, 4);
        inputForNeuralNet.putScalar(new int[]{0, 0}, sensorData.distanceUpNormalized);
        inputForNeuralNet.putScalar(new int[]{0, 1}, sensorData.distanceRightNormalized);
        inputForNeuralNet.putScalar(new int[]{0, 2}, sensorData.distanceDownNormalized);
        inputForNeuralNet.putScalar(new int[]{0, 3}, sensorData.distanceLeftNormalized);
        
        // training memory
        distancesUp.putScalar(new int[]{trainingCounter, 0}, sensorData.distanceUpNormalized);
        trainingData.putRow(trainingCounter++, inputForNeuralNet);
        
        
        //if(trainingCounter == trainingData.rows())
        if(trainingCounter == 5)
        {
        	//DataSet ds = new DataSet(trainingData.getRows(0,1,2,3,4,5,6,7,8,9), trainingData.getRows(1,2,3,4,5,6,7,8,9,10));
        	//DataSet ds = new DataSet(trainingData.getRows(0,1,2,3), trainingData.getRows(1,2,3,4));
        	//DataSet ds = new DataSet(trainingData.getRows(0,1,2,3,4,5,6,7), trainingData.getRows(1,2,3,4,5,6,7,8));
        	DataSet ds = new DataSet(trainingData.getRows(0,1,2,3), distancesUp.getRows(1,2,3,4));
        	
        	
        	neuralNet.fit(ds); 
        	
        	System.out.println("Input:" + trainingData.getRow(0) + " -> Output: " + neuralNet.output(trainingData.getRow(0)));
        	System.out.println("Input:" + trainingData.getRow(1) + " -> Output: " + neuralNet.output(trainingData.getRow(1)));
        	System.out.println("Input:" + trainingData.getRow(2) + " -> Output: " + neuralNet.output(trainingData.getRow(2)));
        	System.out.println("Input:" + trainingData.getRow(3) + " -> Output: " + neuralNet.output(trainingData.getRow(3)));
        	INDArray test = Nd4j.zeros(1, 4).putScalar(new int[]{0, 0}, 0.40).putScalar(new int[]{0, 1}, 0.20).putScalar(new int[]{0, 1}, 0.80).putScalar(new int[]{0, 1}, 0.60);
        	System.out.println("Input:" + test + " -> Output: " + neuralNet.output(test));
        	
        	
        	INDArray output = neuralNet.output(ds.getFeatureMatrix());
        	Evaluation eval = new Evaluation(1);
        	eval.eval(ds.getLabels(), output);
        	
        	System.out.println(output);
        	System.out.println(eval.stats());
        	
        	trainingCounter = 0;
        }
        /*if(previousNeuralNetInputs != null) {
        	DataSet ds = new DataSet(previousNeuralNetInputs, outputsForTraining);
        	//NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler();
            //normalizer.fit(ds);
            //normalizer.transform(ds);
        	neuralNet.fit(ds); 	
        }
        previousNeuralNetInputs = input;*/
        //INDArray result = neuralNet.output(inputForNeuralNet);
        //result = result.mul(SensorPreprocessing.SensorRange);
        //if(Double.isNaN(result.getDouble(0, 0)))
        //	result = null;
	}
	
	private void initNeuralNet()
	{
		// Set up network configuration
        NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
        // how often should the training set be run, we need something above
        // 1000, or a higher learning-rate - found this values just by trial and
        // error
        builder.iterations(500);
        // learning rate
        builder.learningRate(0.1);
        // fixed seed for the random generator, so any run of this program
        // brings the same results - may not work if you do something like
        // ds.shuffle()
        //builder.seed(123);
        // not applicable, this network is to small - but for bigger networks it
        // can help that the network will not only recite the training data
        builder.useDropConnect(false);
        // a standard algorithm for moving on the error-plane, this one works
        // best for me, LINE_GRADIENT_DESCENT or CONJUGATE_GRADIENT can do the
        // job, too - it's an empirical value which one matches best to
        // your problem
        builder.optimizationAlgo(OptimizationAlgorithm.CONJUGATE_GRADIENT);
        // init the bias with 0 - empirical value, too
        builder.biasInit(0);
        // from "http://deeplearning4j.org/architecture": The networks can
        // process the input more quickly and more accurately by ingesting
        // minibatches 5-10 elements at a time in parallel.
        // this example runs better without, because the dataset is smaller than
        // the mini batch size
        builder.miniBatch(false);

        // create a multilayer network with 2 layers (including the output
        // layer, excluding the input payer)
        ListBuilder listBuilder = builder.list();

        DenseLayer.Builder hiddenLayerBuilder = new DenseLayer.Builder();
        // two input connections - simultaneously defines the number of input
        // neurons, because it's the first non-input-layer
        hiddenLayerBuilder.nIn(4);
        // number of outgooing connections, nOut simultaneously defines the
        // number of neurons in this layer
        hiddenLayerBuilder.nOut(16);
        // put the output through the sigmoid function, to cap the output
        // valuebetween 0 and 1
        hiddenLayerBuilder.activation(Activation.SIGMOID);
        // random initialize weights with values between 0 and 1
        hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        hiddenLayerBuilder.dist(new UniformDistribution(0, 1));

        // build and set as layer 1
        listBuilder.layer(0, hiddenLayerBuilder.build());
        

        // MCXENT or NEGATIVELOGLIKELIHOOD (both are mathematically equivalent) work ok for this example - this
        // function calculates the error-value (aka 'cost' or 'loss function value'), and quantifies the goodness
        // or badness of a prediction, in a differentiable way
        // For classification (with mutually exclusive classes, like here), use multiclass cross entropy, in conjunction
        // with softmax activation function
        Builder outputLayerBuilder = new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD);
        // must be the same amout as neurons in the layer before
        outputLayerBuilder.nIn(16);
        // two neurons in this layer
        outputLayerBuilder.nOut(1);
        outputLayerBuilder.activation(Activation.SOFTMAX);
        outputLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        outputLayerBuilder.dist(new UniformDistribution(0, 1));
        listBuilder.layer(1, outputLayerBuilder.build());

        // no pretrain phase for this network
        listBuilder.pretrain(false);

        // seems to be mandatory
        // according to agibsonccc: You typically only use that with
        // pretrain(true) when you want to do pretrain/finetune without changing
        // the previous layers finetuned weights that's for autoencoders and
        // rbms
        listBuilder.backprop(true);

        // build and init the network, will check if everything is configured
        // correct
        MultiLayerConfiguration conf = listBuilder.build();
        neuralNet = new MultiLayerNetwork(conf);
        neuralNet.init();

        // add an listener which outputs the error every 100 parameter updates
        neuralNet.setListeners(new ScoreIterationListener(100));
	}
	
	private void initNeuralNet2()
	{
		// Set up network configuration
        NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
        // how often should the training set be run, we need something above
        // 1000, or a higher learning-rate - found this values just by trial and
        // error
        builder.iterations(10000);
        // learning rate
        builder.learningRate(0.1);
        // fixed seed for the random generator, so any run of this program
        // brings the same results - may not work if you do something like
        // ds.shuffle()
        //builder.seed(123);
        // not applicable, this network is to small - but for bigger networks it
        // can help that the network will not only recite the training data
        builder.useDropConnect(false);
        // a standard algorithm for moving on the error-plane, this one works
        // best for me, LINE_GRADIENT_DESCENT or CONJUGATE_GRADIENT can do the
        // job, too - it's an empirical value which one matches best to
        // your problem
        builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
        // init the bias with 0 - empirical value, too
        builder.biasInit(0);
        // from "http://deeplearning4j.org/architecture": The networks can
        // process the input more quickly and more accurately by ingesting
        // minibatches 5-10 elements at a time in parallel.
        // this example runs better without, because the dataset is smaller than
        // the mini batch size
        builder.miniBatch(false);

        // create a multilayer network with 2 layers (including the output
        // layer, excluding the input payer)
        ListBuilder listBuilder = builder.list();

        DenseLayer.Builder hiddenLayerBuilder = new DenseLayer.Builder();
        // two input connections - simultaneously defines the number of input
        // neurons, because it's the first non-input-layer
        hiddenLayerBuilder.nIn(4);
        // number of outgooing connections, nOut simultaneously defines the
        // number of neurons in this layer
        hiddenLayerBuilder.nOut(32);
        // put the output through the sigmoid function, to cap the output
        // valuebetween 0 and 1
        hiddenLayerBuilder.activation(Activation.SIGMOID);
        // random initialize weights with values between 0 and 1
        hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        hiddenLayerBuilder.dist(new UniformDistribution(0, 1));

        // build and set as layer 1
        listBuilder.layer(0, hiddenLayerBuilder.build());
        
        hiddenLayerBuilder = new DenseLayer.Builder();
        // two input connections - simultaneously defines the number of input
        // neurons, because it's the first non-input-layer
        hiddenLayerBuilder.nIn(32);
        // number of outgooing connections, nOut simultaneously defines the
        // number of neurons in this layer
        hiddenLayerBuilder.nOut(32);
        // put the output through the sigmoid function, to cap the output
        // valuebetween 0 and 1
        hiddenLayerBuilder.activation(Activation.SIGMOID);
        // random initialize weights with values between 0 and 1
        hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        hiddenLayerBuilder.dist(new UniformDistribution(0, 1));

        // build and set as layer 0
        listBuilder.layer(1, hiddenLayerBuilder.build());
        
        hiddenLayerBuilder = new DenseLayer.Builder();
        // two input connections - simultaneously defines the number of input
        // neurons, because it's the first non-input-layer
        hiddenLayerBuilder.nIn(32);
        // number of outgooing connections, nOut simultaneously defines the
        // number of neurons in this layer
        hiddenLayerBuilder.nOut(32);
        // put the output through the sigmoid function, to cap the output
        // valuebetween 0 and 1
        hiddenLayerBuilder.activation(Activation.SIGMOID);
        // random initialize weights with values between 0 and 1
        hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        hiddenLayerBuilder.dist(new UniformDistribution(0, 1));

        // build and set as layer 0
        listBuilder.layer(2, hiddenLayerBuilder.build());

        // MCXENT or NEGATIVELOGLIKELIHOOD (both are mathematically equivalent) work ok for this example - this
        // function calculates the error-value (aka 'cost' or 'loss function value'), and quantifies the goodness
        // or badness of a prediction, in a differentiable way
        // For classification (with mutually exclusive classes, like here), use multiclass cross entropy, in conjunction
        // with softmax activation function
        Builder outputLayerBuilder = new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD);
        // must be the same amout as neurons in the layer before
        outputLayerBuilder.nIn(32);
        // two neurons in this layer
        outputLayerBuilder.nOut(4);
        outputLayerBuilder.activation(Activation.SOFTMAX);
        outputLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        outputLayerBuilder.dist(new UniformDistribution(0, 1));
        listBuilder.layer(3, outputLayerBuilder.build());

        // no pretrain phase for this network
        listBuilder.pretrain(false);

        // seems to be mandatory
        // according to agibsonccc: You typically only use that with
        // pretrain(true) when you want to do pretrain/finetune without changing
        // the previous layers finetuned weights that's for autoencoders and
        // rbms
        listBuilder.backprop(true);

        // build and init the network, will check if everything is configured
        // correct
        MultiLayerConfiguration conf = listBuilder.build();
        neuralNet = new MultiLayerNetwork(conf);
        neuralNet.init();

        // add an listener which outputs the error every 100 parameter updates
        neuralNet.setListeners(new ScoreIterationListener(100));
	}
	
	private void initNeuralNet1()
	{				
		//MultiLayerConfiguration.Builder conf = new NeuralNetConfiguration.Builder();
		NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
		 
		/* Number of optimizazion iterations. Since our dataset is minimal we use one epoch and multiple iterations.
		 * One epoch is one forward pass and one backward pass of all the training examples.
		 */
		builder.iterations(10000);
		 
		/* For the start we want to deal with same neural nets, so we set the seed to a const number. So any run of this program
		brings the same results */
		//builder.seed(123);
		 
		/* Activation function inside a node. */
		//builder.activation(Activation.RELU);
		 
		/* Initial the weights with Xavier method => TODO research */
		//builder.weightInit(WeightInit.XAVIER);
		 
		/* Learning rate of the neural net. Very complex parameter to set. */
		builder.learningRate(0.1);
		 
		builder.seed(123);
		builder.useDropConnect(false);
		builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
        // init the bias with 0 - empirical value, too
		builder.biasInit(0);
		// from "http://deeplearning4j.org/architecture": The networks can
		// process the input more quickly and more accurately by ingesting
		// minibatches 5-10 elements at a time in parallel.
		// this example runs better without, because the dataset is smaller than
		// the mini batch size
		builder.miniBatch(false);
		 
		//builder.regularization(true).l2(0.0001);
		 
		 
		// create a multilayer network with 3 layers (input layer -> 1 hidden layer -> output layer)
        ListBuilder listBuilder = builder.list();
        
        // INPUT LAYER
        DenseLayer.Builder hiddenLayerBuilder = new DenseLayer.Builder();
        // inputs: four range finder data plus next action to perform
        //hiddenLayerBuilder.nIn(5);
        hiddenLayerBuilder.nIn(4);
        // number of outgooing connections, set to the size of the game grid = 15x15
        hiddenLayerBuilder.nOut(16);
        // put the output through the RELU function, to cap the output
        // valuebetween 0 and 1
        //hiddenLayerBuilder.activation(Activation.RELU);
        // random initialize weights with values between 0 and 1
        //hiddenLayerBuilder.weightInit(WeightInit.XAVIER);
        
        hiddenLayerBuilder.activation(Activation.SIGMOID);
        // random initialize weights with values between 0 and 1
        hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        hiddenLayerBuilder.dist(new UniformDistribution(0, 1));

        // build and set as layer 0
        listBuilder.layer(0, hiddenLayerBuilder.build());

        // HIDDEN LAYER
        hiddenLayerBuilder = new DenseLayer.Builder();
        // inputs: four range finder data plus next action to perform
        hiddenLayerBuilder.nIn(16);
        // number of outgooing connections. outputs sensor data for next performed action.
        hiddenLayerBuilder.nOut(4);
        // put the output through the RELU function, to cap the output
        // valuebetween 0 and 1
        //hiddenLayerBuilder.activation(Activation.RELU);
        // random initialize weights with values between 0 and 1
        //hiddenLayerBuilder.weightInit(WeightInit.XAVIER);
        
        hiddenLayerBuilder.activation(Activation.SIGMOID);
        // random initialize weights with values between 0 and 1
        hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        hiddenLayerBuilder.dist(new UniformDistribution(0, 1));

        // build and set as layer 0
        listBuilder.layer(1, hiddenLayerBuilder.build());
        
        // OUTPUT LAYER
        Builder outputLayerBuilder = new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD);
        // must be the same amout as neurons in the layer before
        outputLayerBuilder.nIn(4);
        // two neurons in this layer
        outputLayerBuilder.nOut(4);
        outputLayerBuilder.activation(Activation.SOFTMAX);
        //outputLayerBuilder.weightInit(WeightInit.XAVIER);
        outputLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        outputLayerBuilder.dist(new UniformDistribution(0, 1));
        listBuilder.layer(2, outputLayerBuilder.build());

        
        // no pretrain phase for this network
        listBuilder.pretrain(false);

        // seems to be mandatory
        // according to agibsonccc: You typically only use that with
        // pretrain(true) when you want to do pretrain/finetune without changing
        // the previous layers finetuned weights that's for autoencoders and
        // rbms
        listBuilder.backprop(true);

        // build and init the network, will check if everything is configured
        // correct
        MultiLayerConfiguration conf = listBuilder.build();
        neuralNet = new MultiLayerNetwork(conf);
        neuralNet.init();
	}
	
	private void predictNextInputQLearning(String action, sensorDataDTO sensorData)
	{
		
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		sensorData = new Gson().fromJson(this.getCommunicator().read(SensorPreprocessing.SENSORDATAADDRESS).getValue(), sensorDataDTO.class);
	}
	
	@Override
	protected void executeCustomPostProcessing() throws Exception {
		this.getCommunicator().write(DatapointBuilder.newDatapoint(ACIONADDRESS).setValue(action));
		log.debug("Written action={}", action);
		
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		if (data.containsKey(SENSORDATAADDRESS)==true) {
			log.debug("Inputs received={}", data);
			this.setStart();
		}
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
