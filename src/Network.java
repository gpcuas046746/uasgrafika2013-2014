import java.util.*;


abstract public class Network {

  protected int inputNeuronCount;

  protected int outputNeuronCount;  

  protected double output[];
  
  public final static double NEURON_ON=0.9;

  public final static double NEURON_OFF=0.1;

  protected double totalError;

  protected Random random = new Random(System.currentTimeMillis());


  abstract public void learn ()
  throws RuntimeException;


  abstract void trial(double []input);



  double []getOutput()
  {
    return output;
  }



  static double vectorLength( double v[] )
  {
    double rtn = 0.0 ;
    for ( int i=0;i<v.length;i++ )
      rtn += v[i] * v[i];
    return rtn;
  }


double dotProduct(double vec1[] , double vec2[] )
  {
    int k,v;
    double rtn;

    rtn = 0.0;
    k = vec1.length;

    v = 0;
    while ( (k--)>0 ) {
      rtn += vec1[v] * vec2[v];
      v++;
    }

    return rtn;
  }

  void randomizeWeights( double weight[][] )
  {
    double r ;


    int temp = (int)(3.464101615 / (2. * Math.random() )) ; // SQRT(12)=3.464...

    for ( int y=0;y<weight.length;y++ ) {
      for ( int x=0;x<weight[0].length;x++ ) {
        r = (double) random.nextInt(Integer.MAX_VALUE) + (double) random.nextInt(Integer.MAX_VALUE) -
            (double) random.nextInt(Integer.MAX_VALUE) - (double) random.nextInt(Integer.MAX_VALUE) ;
        weight[y][x] = temp * r ;
      }
    }
  }

}