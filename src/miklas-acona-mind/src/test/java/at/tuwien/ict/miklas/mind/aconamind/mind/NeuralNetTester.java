package at.tuwien.ict.miklas.mind.aconamind.mind;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

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
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import at.tuwien.ict.miklas.aconamind.datatransferobjects.sensorDataDTO;

public class NeuralNetTester {
	
	
	private static LittleGameGenerator game;

	
	public static void main(String[] args) {
		
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		int playGame = 1;
		boolean training = false;
		boolean ui = true;
												  
		game = new LittleGameGenerator(7, 7, 3, 3, 10000);
		game.setObstacle(2, 1);
		game.setObstacle(3, 1);
		game.setObstacle(1, 5);
		game.setObstacle(4, 3);
		game.setObstacle(5, 3);
		game.setObstacle(4, 4);
		
		while(playGame > 0)
		{
			playGame--;
			
			if(training && playGame == 0) {
				training = false;
				ui = true;
			} 
			if(ui)
				System.out.print(game.toString());

			try {
				String str = "";
				if(!training) {
					str = inputReader.readLine();
					playGame = 1;
				} else {
					str = GamePlayer.PlayerAction.getRandomAction().str;
				}
		
				if(str.contains(GamePlayer.PlayerAction.MOVE_FORWARD.str)) {
					game.movePlayer(GamePlayer.PlayerAction.MOVE_FORWARD);
				}
				else if(str.contains(GamePlayer.PlayerAction.TURN_RIGHT.str)) {
					game.movePlayer(GamePlayer.PlayerAction.TURN_RIGHT);
				}
				else if(str.contains(GamePlayer.PlayerAction.MOVE_BACKWARD.str)) {
					game.movePlayer(GamePlayer.PlayerAction.MOVE_BACKWARD);
				}
				else if(str.contains(GamePlayer.PlayerAction.TURN_LEFT.str)) {
					game.movePlayer(GamePlayer.PlayerAction.TURN_LEFT);
				}
				else if(str.contains("x")) {
					playGame = -1;
				}
				else if(str.contains("t")) {
					training = true;
					playGame = 100001;
				}
				else if(str.contains("u")) {
					training = true;
					ui = false;
					playGame = 100001;
				}
				
			}
			catch(Exception e) {
				System.out.println("Game crashed with msg: " + e);
				playGame = -1;
			}
		}
				
				
		/*INDArray input =  Nd4j.zeros(4, 4);
		INDArray labels = Nd4j.zeros(4, 4);
		createDataSet(input, labels);
		DataSet ds = new DataSet(input, labels);
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
        builder.seed(254);
        // not applicable, this network is to small - but for bigger networks it
        // can help that the network will not only recite the training data
        //builder.useDropConnect(false);
        //builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
        //builder.biasInit(0);
        builder.updater(new Nesterovs(0.9));
        // from "http://deeplearning4j.org/architecture": The networks can
        // process the input more quickly and more accurately by ingesting
        // minibatches 5-10 elements at a time in parallel.
        // this example runs better without, because the dataset is smaller than
        // the mini batch size
        //builder.miniBatch(false);

        // create a multilayer network with 2 layers (including the output
        // layer, excluding the input payer)
        ListBuilder listBuilder = builder.list();

        DenseLayer.Builder hiddenLayerBuilder = new DenseLayer.Builder();
        // two input connections - simultaneously defines the number of input
        // neurons, because it's the first non-input-layer
        hiddenLayerBuilder.nIn(4);
        // number of outgooing connections, nOut simultaneously defines the
        // number of neurons in this layer
        hiddenLayerBuilder.nOut(8);
        // put the output through the sigmoid function, to cap the output
        //hiddenLayerBuilder.activation(Activation.SIGMOID);
        //hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        //hiddenLayerBuilder.dist(new UniformDistribution(0, 1));
        hiddenLayerBuilder.activation(Activation.RELU);

        // build and set as layer 0
        listBuilder.layer(0, hiddenLayerBuilder.build());

        //Builder outputLayerBuilder = new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD);  
        Builder outputLayerBuilder = new OutputLayer.Builder(LossFunctions.LossFunction.MSE);  
        outputLayerBuilder.nIn(8);
        outputLayerBuilder.nOut(4);
        //outputLayerBuilder.activation(Activation.SOFTMAX);
        //outputLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        //outputLayerBuilder.dist(new UniformDistribution(0, 1));
        outputLayerBuilder.activation(Activation.IDENTITY);
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
        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        // add an listener which outputs the error every 100 parameter updates
        net.setListeners(new ScoreIterationListener(100));

        // C&P from GravesLSTMCharModellingExample
        // Print the number of parameters in the network (and for each layer)
        Layer[] layers = net.getLayers();
        int totalNumParams = 0;
        for (int i = 0; i < layers.length; i++) {
            int nParams = layers[i].numParams();
            System.out.println("Number of parameters in layer " + i + ": " + nParams);
            totalNumParams += nParams;
        }
        System.out.println("Total number of network parameters: " + totalNumParams);

        // here the actual learning takes place
        net.fit(ds);
        
        System.out.println("Input:" + input.getRow(0) + " -> Should be: " + labels.getRow(0) + " -> Output: " + net.output(input.getRow(0)));
        System.out.println("Input:" + input.getRow(1) + " -> Should be: " + labels.getRow(1) + " -> Output: " + net.output(input.getRow(1)));
        System.out.println("Input:" + input.getRow(2) + " -> Should be: " + labels.getRow(2) + " -> Output: " + net.output(input.getRow(2)));
        System.out.println("Input:" + input.getRow(3) + " -> Should be: " + labels.getRow(3) + " -> Output: " + net.output(input.getRow(3)));

        // create output for every training sample
        INDArray output = net.output(ds.getFeatureMatrix());
        System.out.println(output);

        // let Evaluation prints stats how often the right output had the
        // highest value
        Evaluation eval = new Evaluation(2);
        eval.eval(ds.getLabels(), output);
        System.out.println(eval.stats());*/
    }
	
	/*private static void createDataSet(INDArray input, INDArray labels) {
		
		input.putScalar(new int[]{0, 0}, 0.2);
		input.putScalar(new int[]{0, 1}, 0.4);
		input.putScalar(new int[]{0, 2}, 0.6);
		input.putScalar(new int[]{0, 3}, 0.8);
		
		labels.putScalar(new int[]{0, 0}, 0.4);
		labels.putScalar(new int[]{0, 1}, 0.6);
		labels.putScalar(new int[]{0, 2}, 0.8);
		labels.putScalar(new int[]{0, 3}, 0.2);
		
		//rotate right
		input.putScalar(new int[]{1, 0}, 0.4);
		input.putScalar(new int[]{1, 1}, 0.6);
		input.putScalar(new int[]{1, 2}, 0.8);
		input.putScalar(new int[]{1, 3}, 0.2);
		
		labels.putScalar(new int[]{1, 0}, 0.6);
		labels.putScalar(new int[]{1, 1}, 0.8);
		labels.putScalar(new int[]{1, 2}, 0.2);
		labels.putScalar(new int[]{1, 3}, 0.4);
		
		//rotate right
		input.putScalar(new int[]{2, 0}, 0.6);
		input.putScalar(new int[]{2, 1}, 0.8);
		input.putScalar(new int[]{2, 2}, 0.2);
		input.putScalar(new int[]{2, 3}, 0.4);
		
		labels.putScalar(new int[]{2, 0}, 0.8);
		labels.putScalar(new int[]{2, 1}, 0.2);
		labels.putScalar(new int[]{2, 2}, 0.4);
		labels.putScalar(new int[]{2, 3}, 0.6);
		
		input.putScalar(new int[]{3, 0}, 0.8);
		input.putScalar(new int[]{3, 1}, 0.2);
		input.putScalar(new int[]{3, 2}, 0.4);
		input.putScalar(new int[]{3, 3}, 0.6);
		
		labels.putScalar(new int[]{3, 0}, 0.2);
		labels.putScalar(new int[]{3, 1}, 0.4);
		labels.putScalar(new int[]{3, 2}, 0.6);
		labels.putScalar(new int[]{3, 3}, 0.8);
	}*/
	
	
	
	static class LittleGameGenerator {
		private int[][] gameField;
		private GamePlayer gamePlayer;

		private INDArray input;
		private INDArray labels;
		private INDArray prediction;
		private DataSet ds;
		private MultiLayerNetwork neuralNet;
		private int memoryCounter;
		
		public LittleGameGenerator(int xSize, int ySize, int xPlayer, int yPlayer, int memorySize) {
			gameField = new int[ySize][xSize];
			generateWalls();
			
			gamePlayer = new GamePlayer(xPlayer, yPlayer, GamePlayer.PlayerDirection.Up, gameField);
			
			//neural net initialisations
			createNeuralNet();
			input = Nd4j.zeros(memorySize, 5);
			labels = Nd4j.zeros(memorySize, 4);
			prediction = Nd4j.ones(1, 4).mul(gamePlayer.SensorRange + 1);
			memoryCounter = 0;
		}
		
		private void createNeuralNet() {

	        NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
	        builder.iterations(1000);
	        builder.learningRate(0.1);
	        builder.seed(254);
	        builder.updater(new Nesterovs(0.9));
	        ListBuilder listBuilder = builder.list();

	        DenseLayer.Builder hiddenLayerBuilder = new DenseLayer.Builder();
	        hiddenLayerBuilder.nIn(5);
	        hiddenLayerBuilder.nOut(49*2);
	        hiddenLayerBuilder.activation(Activation.RELU);
	        listBuilder.layer(0, hiddenLayerBuilder.build());

	        Builder outputLayerBuilder = new OutputLayer.Builder(LossFunctions.LossFunction.MSE);  
	        outputLayerBuilder.nIn(49*2);
	        outputLayerBuilder.nOut(4);
	        outputLayerBuilder.activation(Activation.IDENTITY);
	        listBuilder.layer(1, outputLayerBuilder.build());

	        listBuilder.pretrain(false);
	        listBuilder.backprop(true);

	        MultiLayerConfiguration conf = listBuilder.build();
	        neuralNet = new MultiLayerNetwork(conf);
	        neuralNet.init();
	        neuralNet.setListeners(new ScoreIterationListener(100));
		}
		
		private void generateWalls() {
			for(int x = 0; x < gameField[0].length; x++)
				for(int y = 0; y < gameField.length; y += gameField.length - 1)
					gameField[x][y] = 1;
			
			for(int x = 0; x < gameField[0].length; x+=  gameField[0].length - 1)
				for(int y = 0; y < gameField.length; y++)
					gameField[x][y] = 1;
		}
		
		private INDArray generateNeuralNetInput(GamePlayer.PlayerAction action) {
			INDArray result = Nd4j.zeros(1, 5);
			generateNeuralNetLabels(result);
			result.putScalar(new int[]{0, 4}, action.value);
			return result;
		}
		private INDArray generateNeuralNetLabels() {
			INDArray result = Nd4j.zeros(1, 4);
			generateNeuralNetLabels(result);
			return result;
		}
		private void generateNeuralNetLabels(INDArray array) {
			array.putScalar(new int[]{0, 0}, getSensorData().distanceUpNormalized);
			array.putScalar(new int[]{0, 1}, getSensorData().distanceRightNormalized);
			array.putScalar(new int[]{0, 2}, getSensorData().distanceDownNormalized);
			array.putScalar(new int[]{0, 3}, getSensorData().distanceLeftNormalized);
		}
		
		public void predictNextSensorData(GamePlayer.PlayerAction action) {
			
			INDArray output = predictNextSensorData(generateNeuralNetInput(action));
			output.mul(gamePlayer.SensorRange);
		}
		
		public INDArray predictNextSensorData(INDArray input) {
			return neuralNet.output(input);
		}
		
		public void movePlayer(GamePlayer.PlayerAction action) {
			INDArray neuralNetInput = generateNeuralNetInput(action);
			prediction = predictNextSensorData(neuralNetInput);
			input.putRow(memoryCounter, generateNeuralNetInput(action));
			gamePlayer.performAction(action);
			labels.putRow(memoryCounter++, generateNeuralNetLabels());
			
			// TRAINING
			if(memoryCounter == input.rows()) {
				ds = new DataSet(input, labels);
				neuralNet.fit(ds);
				memoryCounter = 0;
			}
		}
		
		public void setObstacle(int x, int y) {
			gameField[y][x] = 1;	
			gamePlayer.calculateSensorData();
		}
		
		public sensorDataDTO getSensorData() {
			return gamePlayer.getSensorData();
		}
		
		@Override
		public String toString() {
			String result = "";
			
			for(int y = 0; y < gameField.length; y++)
				for(int x = 0; x < gameField[0].length; x++) {
					if(x == gamePlayer.x && y == gamePlayer.y)
						result += Character.toString((char)gameField[y][x]) + ' ';
					else if(x != gameField[0].length - 1)
						result += Integer.toString(gameField[y][x]) + ' ';
					else
					{
						result += Integer.toString(gameField[y][x]) + '\t';
						
						// print sensor data
						//for(int yy = 0; yy < gameField.length; yy++)
							for(int xx = 0; xx < gameField[0].length; xx++)
							{
								if(gamePlayer.locationUp.x == xx && gamePlayer.locationUp.y == y)
									result += "1";
								else if(gamePlayer.locationRight.x == xx && gamePlayer.locationRight.y == y)
									result += "1";
								else if(gamePlayer.locationDown.x == xx && gamePlayer.locationDown.y == y)
									result += "1";
								else if(gamePlayer.locationLeft.x == xx && gamePlayer.locationLeft.y == y)
									result += "1";
								else if(xx == gamePlayer.x && y == gamePlayer.y)
									result += Character.toString((char)gameField[y][xx]);
								else 
									result += "0";
								
								if(xx != gameField[0].length - 1)
									result += ' ';
								else
									result += '\n';
							}
					}
				}
			
			prediction = prediction.mul(gamePlayer.SensorRange);
			result += "Distance up:\t" + getSensorDataAsString(getSensorData().distanceUp) + "\t prediction: "+ getSensorDataAsString(prediction.getDouble(0)) + "\n";
			result += "Distance right:\t" + getSensorDataAsString(getSensorData().distanceRight) + "\t prediction: "+ getSensorDataAsString(prediction.getDouble(1)) + "\n";
			result += "Distance down:\t" + getSensorDataAsString(getSensorData().distanceDown) + "\t prediction: "+ getSensorDataAsString(prediction.getDouble(2)) + "\n";
			result += "Distance left:\t" + getSensorDataAsString(getSensorData().distanceLeft) + "\t prediction: "+ getSensorDataAsString(prediction.getDouble(3)) + "\n";
			return result;
		}
		
		private String getSensorDataAsString(double value)
		{
			return getSensorDataAsString((value >= 0 && value < gamePlayer.SensorRange) ? (int) Math.round(value) : Integer.MAX_VALUE);
		}
		
		private String getSensorDataAsString(int value)
		{
			return ((value >= 0 && value < gamePlayer.SensorRange) ? Integer.toString(value) : Character.toString((char) 8734));
		}
	}
		
	static class GamePlayer {
		public enum PlayerDirection {Up(8593), Right(8594), Down(8595), Left(8592);
			public int value;
			PlayerDirection(int value) { this.value = value; }};
		public enum PlayerAction {MOVE_FORWARD(1.0, "w"), MOVE_BACKWARD(0.5, "s"), TURN_LEFT(0.25, "a"), TURN_RIGHT(0.75, "d");
			public double value;
			public String str;
			PlayerAction(double value, String str) { this.value = value; this.str = str;}
			public static PlayerAction getRandomAction(){ return PlayerAction.values()[new Random().nextInt(PlayerAction.values().length)]; }};
		public int SensorRange = 2;  
		
		private int x;
		private int y;
		private int [][] gameField;
		private PlayerDirection direction;
		private sensorDataDTO sensorData = new sensorDataDTO();
		
		private Point locationUp, locationDown, locationRight, locationLeft;

		public GamePlayer(int x, int y, PlayerDirection direction, int gameField[][]) {
			this.x = x;
			this.y = y;
			this.gameField = gameField;
			this.direction = direction;
			this.gameField[y][x] = direction.value;
			this.locationUp = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
			this.locationDown = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
			this.locationRight = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
			this.locationLeft = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
			
			calculateSensorData();
		}
		
		public void performAction(PlayerAction action) {
			movePlayer(action);
			calculateSensorData();
		}
		
		public sensorDataDTO getSensorData() {
			return sensorData;
		}
		
		private void movePlayer(PlayerAction action) {
			this.gameField[this.y][this.x] = 0;
			switch(action)
			{
			case MOVE_FORWARD:
				if(sensorData.distanceUp != 0)
					switch(direction) {
					case Up: 	this.y = this.y - 1; break;
					case Right: this.x = this.x + 1; break;
					case Down:	this.y = this.y + 1; break;
					case Left:	this.x = this.x - 1; break;
					}
				break;
			case MOVE_BACKWARD:
				if(sensorData.distanceDown != 0)
					switch(direction) {
					case Up: 	this.y = this.y + 1; break;
					case Right: this.x = this.x - 1; break;
					case Down:	this.y = this.y - 1; break;
					case Left:	this.x = this.x + 1; break;
					}
				break;
			case TURN_LEFT:
				switch(direction) {
				case Up: 	direction = PlayerDirection.Left; break;
				case Right: direction = PlayerDirection.Up; break;
				case Down:	direction = PlayerDirection.Right; break;
				case Left:	direction = PlayerDirection.Down; break;
				}
				break;
			case TURN_RIGHT:
				switch(direction) {
				case Up: 	direction = PlayerDirection.Right; break;
				case Right: direction = PlayerDirection.Down; break;
				case Down:	direction = PlayerDirection.Left; break;
				case Left:	direction = PlayerDirection.Up; break;
				}
				break;
			}
			this.gameField[this.y][this.x] = direction.value;
		}
		
		public void calculateSensorData()
		{
			switch(direction) {
			case Up:
				sensorData.distanceUp = getSensorDataUp(locationUp);
				sensorData.distanceRight = getSensorDataRight(locationRight);
				sensorData.distanceDown = getSensorDataDown(locationDown);
				sensorData.distanceLeft = getSensorDataLeft(locationLeft);
				break;
			case Right:
				sensorData.distanceUp = getSensorDataRight(locationUp);
				sensorData.distanceRight = getSensorDataDown(locationRight);
				sensorData.distanceDown = getSensorDataLeft(locationDown);
				sensorData.distanceLeft = getSensorDataUp(locationLeft);
				break;
			case Down:
				sensorData.distanceUp = getSensorDataDown(locationUp);
				sensorData.distanceRight = getSensorDataLeft(locationRight);
				sensorData.distanceDown = getSensorDataUp(locationDown);
				sensorData.distanceLeft = getSensorDataRight(locationLeft);
				break;
			case Left:
				sensorData.distanceUp = getSensorDataLeft(locationUp);
				sensorData.distanceRight = getSensorDataUp(locationRight);
				sensorData.distanceDown = getSensorDataRight(locationDown);
				sensorData.distanceLeft = getSensorDataDown(locationLeft);
				break;
			}
			sensorData.normalizeFeatureScaling(SensorRange, 0);
		}
		
		private int getSensorDataUp(Point p) {
			int value = Integer.MAX_VALUE;
			p.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
			for(int y = this.y - 1; y >= 0; y--)
				if(this.y - y > SensorRange)
					break;
				else if(gameField[y][this.x] == 1) {
					value = this.y - y - 1;
					p.setLocation(this.x, y);
					break;
				}
			return value;
		}
		
		private int getSensorDataRight(Point p) {
			p.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
			int value = Integer.MAX_VALUE;
			for(int x = this.x + 1; x >= 0; x++)
				if(x - this.x > SensorRange)
					break;
				else if(gameField[this.y][x] == 1) {
					value = x - this.x - 1;
					p.setLocation(x, this.y);
					break;
				}
			return value;
		}
		
		private int getSensorDataDown(Point p) {
			p.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
			int value = Integer.MAX_VALUE;
			for(int y = this.y + 1; y >= 0; y++) 
				if(y - this.y > SensorRange) 
					break;
				else if(gameField[y][this.x] == 1) {
					value = y - this.y - 1;
					p.setLocation(this.x, y);
					break;
				}
			return value;
		}
		
		private int getSensorDataLeft(Point p) {
			p.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
			int value = Integer.MAX_VALUE;
			for(int x = this.x - 1; x >= 0; x--)
				if(this.x - x > SensorRange)
					break;
				else if(gameField[this.y][x] == 1) {
					value = this.x - x - 1;
					p.setLocation(x, this.y);
					break;
				}
			return value;
		}
	}	
}
