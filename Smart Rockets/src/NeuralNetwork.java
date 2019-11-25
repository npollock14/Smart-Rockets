
public class NeuralNetwork {
		int[] nodes;
		Matrix[] weights;
		Matrix[] biases;
		Matrix[] layers;
		double learningRate = .5;

		public NeuralNetwork(int... nodes) {
			this.nodes = nodes;
			layers = new Matrix[nodes.length];
			weights = new Matrix[nodes.length - 1];
			biases = new Matrix[nodes.length - 1];
			for (int i = 0; i < weights.length; i++) {
				weights[i] = Matrix.random(nodes[i + 1], nodes[i]);
				biases[i] = Matrix.random(nodes[i + 1], 1);
			}

		}

		public NeuralNetwork(NeuralNetwork nn) {
			this.nodes = nn.nodes;
			Matrix[] newWeights = new Matrix[nn.weights.length];
			for (int i = 0; i < nn.weights.length; i++) {
				newWeights[i] = new Matrix(nn.weights[i]);
			}
			this.weights = newWeights;
			Matrix[] newBiases = new Matrix[nn.biases.length];
			for (int i = 0; i < nn.biases.length; i++) {
				newBiases[i] = new Matrix(nn.biases[i]);
			}
			this.biases = newBiases;
			Matrix[] newLayers = new Matrix[nn.layers.length];
			for (int i = 0; i < nn.layers.length; i++) {
				newLayers[i] = new Matrix(nn.layers[i]);
			}
			this.layers = newLayers;
			this.learningRate = nn.learningRate;
		}

		public Matrix feedFoward(Matrix input) {
			layers[0] = input;
			for (int i = 0; i < nodes.length - 1; i++) {
				layers[i + 1] = weights[i].timesM(layers[i]).plus(biases[i]).mapSigmoid();
			}
			return layers[layers.length - 1];
		}

		public void train(Matrix inputs, Matrix targets) {

			feedFoward(inputs); // now we have all the layers stuff

// start back propagating
			Matrix[] errors = new Matrix[nodes.length - 1];

			errors[0] = targets.minus(layers[layers.length - 1]); // targets - outputs = error
			for (int i = 0; i < nodes.length - 1; i++) { // back propagates through the layers
				if (i != 0) {
// new error = prev weights transposed times prev errors
					errors[i] = weights[nodes.length - 1 - i].transpose().timesM(errors[i - 1]);
				}
				Matrix gradient = layers[nodes.length - 1 - i].mapDsigmoid();
				gradient.times(errors[i]);
				gradient.scalarMult(learningRate);
				Matrix deltaWs = gradient.timesM(layers[nodes.length - 2 - i].transpose());
				weights[weights.length - 1 - i] = weights[weights.length - 1 - i].plus(deltaWs);
				biases[biases.length - 1 - i] = biases[biases.length - 1 - i].plus(gradient);
			}

		}
	}

