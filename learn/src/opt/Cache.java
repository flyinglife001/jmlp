package opt;

import java.util.Iterator;
import java.util.LinkedList;
import kernel.Kernel;
import kernel.PolynomialKernel;
import core.DataSet;
import core.OutFile;


public class Cache
{
	private DataSet _data;
	private Kernel _K;
	private int _n_cache,_n_dim,_size;
	private  LinkedList<Entry> _cache_list = new LinkedList<Entry>();
	
	class Entry
	{
		int _row_index;
		double[] _vect;
	}
	
	public Cache(DataSet data, Kernel kernel, int cache_size)
	{
		_data = data;
		_K = kernel;
		_size = cache_size;
		
		initialize_cache();
	}
	
	//since shrinking, the data object will shrink to small. so it need to fill X.
	void initialize_cache()
	{
		//Entry object contains 64*n + 32;
		_n_dim = _data._n_rows;
		_n_cache = (int)(_size*1048576.0/(_n_dim*64 + 32));
		
		OutFile.printf("the size of the cache: %d\n", _n_cache);
		
		_n_cache = Math.min(_n_cache, _n_dim);
		
		//initial the cache lists.
		int i,j;
		double[] x;
		for(i = 0; i < _n_cache; i++)
		{
			Entry entry = new Entry();
			entry._row_index = i;
			double[] vect = new double[_n_dim];
			
			x = _data.get_X(i);
			for(j = 0; j < _n_dim; j++)
			{
				vect[j] = _K.eval(x, _data.get_X(j));
			}
			
			entry._vect = vect;
			
			_cache_list.add(entry);
		}
	}
	
	void print_cache_list()
	{
		int i;
		for(i = 0; i < _cache_list.size(); i++)
		{
			Entry en = _cache_list.get(i);
			OutFile.printf("row: %d %f\n",en._row_index,en._vect[0]);
		}
	}
	
	int get_dim()
	{
		return _n_dim;
	}
	
	double[] getX(int row)
	{
		double[] feature = null;
		Iterator<Entry> it = _cache_list.iterator();
		Entry hit = null;
		while(it.hasNext())
		{
			Entry entry = it.next();
			if(entry._row_index == row)
			{
				hit = entry;
				_cache_list.remove(hit);
				_cache_list.addFirst(hit);
				break;
			}
		}
		
		if(hit == null)
		{
			_cache_list.removeLast();
			Entry entry = new Entry();
			entry._row_index = row;
			double[] vect = new double[_n_dim];
			
			double[] x = _data.get_X(row);
			for(int j = 0; j < _n_dim; j++)
			{
				vect[j] = _K.eval(x, _data.get_X(j));
			}
			
			entry._vect = vect;
			_cache_list.addFirst(entry);
			
			feature = vect;
		}
		else
		{
			feature = hit._vect;
		}
		
		return feature;
	}
	
	
	public static void main(String[] args)
	{
		DataSet data = new DataSet(true);
		data.load_file("D:\\UCIDataSet\\Segment\\train_data.dat");
		PolynomialKernel k = new PolynomialKernel(1, 0, 1);
		
		int rows = data._n_rows;
		int i,j;
		
		Cache ca = new Cache(data,k,1);
		
		
		for(i = 0; i < 20; i++)
		{
			OutFile.printf("%f\n",ca.getX(i)[0]);
			ca.print_cache_list();
		}
		
	}
	
}
