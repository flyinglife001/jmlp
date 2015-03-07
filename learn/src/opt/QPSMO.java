package opt;

import kernel.Kernel;
import kernel.PolynomialKernel;
import core.DataSet;
import core.OutFile;
import core.Utility;
import cmd.General;

public class QPSMO
{
	/**
	 * Solve the QP problem as following: min_{a} 1/2 a^T Q a - e^T a <br>
	 * s.t. 0 <= a_i <= C and y^T a = 0
	 * */
	
	private Cache _cache;
	private int[] _y;
	private double[] _G;
	private double[] _A;
	private int _i, _j, _length;
	private double _C, _tau, _eps;
	private DataSet _data;

	public QPSMO(DataSet data, Kernel k, int cache_size, double C)
	{
		_data = data;
		
		_C = C;
		_tau = 1e-12;
		_eps = 1e-3;
		
		initialize();
	}
	
	private void initialize()
	{
		//due to the data change, then need to refresh the cache.
		_cache.initialize_cache();
		_length = _cache.get_dim();
		_G = new double[_length];
		Utility.assign(_G, -1);
		_A = new double[_length];
		fetch_y();
	}
	
	private void fetch_y()
	{
		int label,i,row = _data._n_rows;
		_y = new int[row];
		for(i = 0; i < row; i++)
		{
			label = _data.get_label(i);
			if(label == 0)
				_y[i] = -1;
			else
				_y[i] = +1;
		}
	}
	
	private void shrinking()
	{
		int i,j;
		
		//compute \lambda_eq = \frac{1}{|A|} \sum_{i \ in A} {y_i - \sum_{j = 1}^l \alpha_i y_j k(x_i,x_j)}
		int n_actives = 0;
		double lambda_eq = 0,kernel_sum;
		double[] feature;
		double[] lambda_low = new double[_length];
		for(i = 0; i < _length; i++)
		{
			if(_A[i] > 0 && _A[i] < _C)
			{
				kernel_sum = 0;
				
				feature = _cache.getX(i);
				for(j = 0; j < _length; j++)
				{
					kernel_sum += _A[i]*_y[i]*feature[j];
				}
				lambda_eq += _y[i] - kernel_sum;
				
				n_actives++;
			}
			else if(_C - _A[i] < 1e-6)
			{
				
			}
		}
		
		//compute \lambda_{low} and \lambda_{up} 
		for(i = 0; i < _length; i++)
		{
			kernel_sum = 0;
			feature = _cache.getX(i);
			for(j = 0; j < _length; j++)
			{
				if(_A[j] > 0)
				{
					kernel_sum += _A[i];
				}
			}
		}
		
	}

	public double[] optimize()
	{
		double a,b,ba;
		double oldAi,oldAj,sum;
		int t;
		double[] feature;
		int iteration = 0;
		while (true)
		{
			select();
			if (_j == -1)
				break;

			feature = _cache.getX(_i);
			a = feature[_i] + _cache.getX(_j)[_j] - 2
					* _y[_i] * _y[_j]
					* feature[_j];
			if(a <= 0)
				a = _tau;
			
			b = -_y[_i]*_G[_i] + _y[_j]*_G[_j];
			
			//update alpha
			oldAi = _A[_i];
			oldAj = _A[_j];
			ba = b/a;
			_A[_i] += _y[_i]*ba;
			_A[_j] -= _y[_j]*ba;
			
			//project alpha back to the feasible region.
			sum = _y[_i]*oldAi + _y[_j]*oldAj;
			if(_A[_i] > _C)
				_A[_i] = _C;
			if(_A[_i] < 0)
				_A[_i] = 0;
			_A[_j] = _y[_j]*(sum - _y[_i]*_A[_i]);
			
			if(_A[_j] > _C)
				_A[_j] = _C;
			if(_A[_j] < 0)
				_A[_j] = 0;
			_A[_i] = _y[_i]*(sum - _y[_j]*_A[_j]);
			
			//update the gradient with delta values. 
			//delataAi = A[i] - oldAi; deltaAj = A[j] - oldAj;
			for(t = 0; t < _length; t++)
			{
				feature = _cache.getX(t);
				_G[t] += feature[_i]*(_A[_i] - oldAi) + feature[_j]*(_A[_j] - oldAj);
			}
			
			iteration++;
			
			if(iteration%100 == 0)
				shrinking();
			
			
		}
		
		return _A;
	}

	private void select()
	{
		// select i;
		_i = -1;
		double G_max = -Double.MAX_VALUE;
		double G_min = Double.MAX_VALUE;
		int t;
		double yg,ba;
		double[] feature;
		for (t = 0; t < _length; t++)
		{
			if ((_y[t] == +1 && _A[t] < _C)
					|| (_y[t] == -1 && _A[t] > 0))
			{
				if (-_y[t] * _G[t] >= G_max)
				{
					_i = t;
					G_max = -_y[t] * _G[t];
				}
			}
		}

		// select j;
		_j = -1;
		double obj_min = Double.MAX_VALUE;
		double b = 0, a = 0;
		for (t = 0; t < _length; t++)
		{
			if ((_y[t] == +1 && _A[t] > 0)
					|| (_y[t] == -1 && _A[t] < _C))
			{
				yg = _y[t] * _G[t];
				b = G_max + yg;
				if (-yg <= G_min)
				{
					G_min = -yg;
				}
				if (b > 0)
				{
					feature = _cache.getX(_i);
					a = feature[_i] + _cache.getX(t)[t] - 2
							* _y[_i] * _y[t] * feature[t];
					if (a <= 0)
						a = _tau;

					ba = -(b * b) / a;
					if (ba <= obj_min)
					{
						_j = t;
						obj_min = ba;
					}
				}
			}
		}

		if (G_max - G_min < _eps)
		{
			_i = -1;
			_j = -1;
		}
		
		//OutFile.printf("i: %d j: %d\n",_i,_j);
	}
	
	public static void main(String[] args)
	{
		DataSet data = new DataSet(true);
		data.load_file("D:\\UCIDataSet\\Pima\\train_data.dat");
		PolynomialKernel k = new PolynomialKernel(1, 0, 1);
		QPSMO qp = new QPSMO(data,k,1,100);
		
		
		double[] solution = qp.optimize();
		OutFile.printf("the solution is: \n");
		int i;
		for(i = 0; i < solution.length; i++)
		{
			if(i%10 == 0)
				OutFile.printf("\n");
			
			OutFile.printf("%f ",solution[i]);
		}
		
		
	}
	
}
