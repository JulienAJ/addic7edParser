package julienaj.addictedParser;

public class Pair<T extends Comparable>
{
	T key;
	T value;

	public Pair(T key, T value)
	{
		this.key = key;
		this.value = value;
	}

	public T getKey()
	{
		return key;
	}

	public T getValue()
	{
		return value;
	}
}
