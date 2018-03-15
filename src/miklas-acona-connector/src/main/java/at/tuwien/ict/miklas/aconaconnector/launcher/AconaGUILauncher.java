/**
 * Class GUILauncher
 * starts Miklas with a start JFrame 
 */

package at.tuwien.ict.miklas.aconaconnector.launcher;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigLoader;
import evaluator.EvaluatorManager;
import evaluator.EvaluatorManagerMindInterface;
import gameengine.GameEngineImpl;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import userinterface.MiklasSettings;
import userinterface.MusicManager;
import userinterface.MusicManagerImpl;
import userinterface.SoundManager;
import userinterface.SoundManagerImpl;
import userinterface.Visualization;

public class AconaGUILauncher extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private ConfigLoader conf = null;
	private Visualization vis;
	private GameEngineImpl ge;
	private SoundManager sm;
	private MusicManager mm;
	private EvaluatorManagerMindInterface emmi;
	
	private JLabel banner;
	
	private static final Logger log = LoggerFactory.getLogger(AconaGUILauncher.class);

	@Override
	public void actionPerformed(ActionEvent ae) {
		switch(ae.getActionCommand()) {
		case "settings":
			MiklasSettings ms = new MiklasSettings(this);
			ms.setVisible(true);
			try {
				conf.init();
				if(!conf.getBannerImagePath().isEmpty())
					banner.setIcon(new ImageIcon(conf.getBannerImagePath()));
				else
					banner.setText("Current game: " + conf.getGameTitle());
				pack();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			break;
		case "newgame":		// on starting new game
			// instantiate game engine and visualization, etc.
			ge = new GameEngineImpl();
			vis = new Visualization(this);
			emmi = new EvaluatorManager(vis);
			sm = new SoundManagerImpl(ge.getGameGrid());
			mm = new MusicManagerImpl();
			
			// init game
			ge.setSoundManager(sm);
			ge.setScoreManager(emmi);
			ge.setMusicManager(mm);
			ge.init();
			this.setVisible(false);
			
			// start visualization
		    SwingUtilities.invokeLater(new Runnable() {
		    	public void run() {
		    		vis.setGameEngine(ge);
		    		vis.setTitle(conf.getGameTitle());
		    		vis.init();
		    		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		    		vis.setLocation(dim.width/2-vis.getSize().width/2, dim.height/2-vis.getSize().height/2);
		    		vis.setVisible(true);
		    	}
		    });
			break;
		case "quit":
			System.exit(0);
			break;
		}
	}
	
	public AconaGUILauncher() {
		super("Miklas");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	/**
	 * frame initialization
	 */
	public void init() {
		JButton bNewGame, bSettings, bQuit;
		
		bNewGame = new JButton("New Game");
		bSettings = new JButton("Settings");
		bQuit = new JButton("Quit");
		bNewGame.setActionCommand("newgame");
		bSettings.setActionCommand("settings");
		bQuit.setActionCommand("quit");
		bNewGame.addActionListener(this);
		bSettings.addActionListener(this);
		bQuit.addActionListener(this);
		
		if(!conf.getBannerImagePath().isEmpty())
			banner = new JLabel(new ImageIcon(conf.getBannerImagePath()));
		else
			banner = new JLabel("Current game: " + conf.getGameTitle());
		
		setLayout(new GridLayout(4,1,3,3));
		add(banner);
		add(bNewGame);
		add(bSettings);
		add(bQuit);
		pack();
		
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * 
	 * @param args program arguments
	 */
	public static void main(String[] args) {
		log.info("Start Miklas");
		
		AconaGUILauncher launcher = new AconaGUILauncher();
		
		try {
			launcher.conf = ConfigLoader.getConfig();
			launcher.conf.init();
		}
		catch(Exception e) {
			log.error("Configuration couldn't be loaded. Program exists.");
			System.exit(-1);
		}
		
		
		launcher.init();
	}

}
