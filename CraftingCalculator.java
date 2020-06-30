/* example recipe file:
1x Undyed Hempen Cloth
$time = 1
* Weaving
> 2x Hempen Yarn
> 1x Lightning Shard

2x Hempen Yarn
$time = 1
* Weaving
> 2x Moko Grass
> 1x Lightning Shard

1x Moko Grass
$time = 2
* Harvesting
*/

/* example crafting list:
50x Undyed Hempen Cloth
1x Hempen Yarn
*/

import java.io.File;
import java.util.Scanner;



public class CraftingCalculator
{
	public static void main(String[] args) throws Exception
	{
		Recipe[] recipes;
		if(args.length == 0)
			recipes = getRecipes(userString("Please specify a file to get recipes from: "));
		else if(args.length == 1)
			recipes = getRecipes(args[0]);
		else
			throw new Exception("Wrong number of arguments");

		if(recipes == null)
			throw new Exception("Could not find the specified file");

		connect(recipes);

		if(loopy(recipes))
			throw new Exception("Loop detected: " + arrayString(loop(recipes)));

		while(true)
		{
			boolean fromFile = !(userString("Do you want to import the crafting list from a text file? (Y/n) ").equalsIgnoreCase("n"));

			Recipe craftingList;
			if(fromFile)
			{
				craftingList = getList(userString("Please specify the file to get the crafting list from: "));
			}
			else
			{
				craftingList = getList();
			}

			connect(craftingList, recipes);

			System.out.println();
			System.out.println(craftingList);

			if(userString("Do you want to do craft more items using the same list of recipes? (Y/n) ").equalsIgnoreCase("n"))
				break;
		}
	}

	public static String userString(String message)
	{
		System.out.print(message);

		Scanner scanner = new Scanner(System.in);
		return scanner.nextLine();
	}

	public static Recipe getList()
	{
		String line = "";
		Recipe list = new Recipe("Crafting list", 1);
		try
		{
			boolean nextIngredient = true;
			while(nextIngredient)
			{
				line = userString("Provide quantity and ingredient on the same form as this example:\n\t5x Wooden plank\n\t");

				String quantity = line.split("x", 2)[0].trim();
				String ingredient = line.split("x", 2)[1].trim();
				list.ingredient(ingredient, Integer.parseInt(quantity));

				nextIngredient = !(userString("Do you want to add another ingredient? (Y/n) ").equalsIgnoreCase("n"));
			}
		}
		catch(Exception e)
		{
			return null;
		}

		if(list.ingredientNames().length == 0)
			return null;
		else
			return list;
	}

	public static Recipe getList(String fileName)
	{
		File file;
		try { file = new File(fileName); }
		catch(Exception e)
		{
			return null;
		}

		Scanner scanner;
		try { scanner = new Scanner(file); }
		catch(Exception e)
		{
			return null;
		}

		String line = "";
		Recipe list = new Recipe("Crafting list", 1);
		try
		{
			while(scanner.hasNextLine())
			{
				line = scanner.nextLine().trim();

				String quantity = line.split("x", 2)[0].trim();
				String ingredient = line.split("x", 2)[1].trim();

				list.ingredient(ingredient, Integer.parseInt(quantity));
			}
		}
		catch(Exception e)
		{
			System.out.println("Something went wrong while reading line: " + line);
			return null;
		}

		if(list.ingredientNames().length == 0)
			return null;
		else
			return list;
	}

	public static Recipe[] getRecipes(String fileName)
	{
		File file;
		try { file = new File(fileName); }
		catch(Exception e)
		{
			return null;
		}

		Scanner scanner;
		try { scanner = new Scanner(file); }
		catch(Exception e)
		{
			return null;
		}

		String line = "";
		Recipe[] recipes = new Recipe[0];
		try
		{
			while(scanner.hasNextLine())
			{
				line = scanner.nextLine().trim();

				if((line.trim().isEmpty()) || (line.startsWith("//")))
				{
					continue;
				}
				else if(line.startsWith("$"))
				{
					line = line.substring(1).trim();
					switch(line.split("=")[0].trim().toLowerCase())
					{
						case("time"):
							recipes[recipes.length - 1].time(Integer.parseInt(line.split("=")[1].trim()));
						break;
					}
				}
				else if(line.startsWith("*"))
				{
					line = line.substring(1).trim();
					recipes[recipes.length - 1].description(line);
				}
				else if(line.startsWith(">"))
				{
					line = line.substring(1).trim();
					String[] splitLine =  line.split("x", 2);
					recipes[recipes.length - 1].ingredient(splitLine[1].trim(), Integer.parseInt(splitLine[0]));
				}
				else if(line.split("x")[0].matches("[-+]?\\d*"))
				{
					line = line.trim();
					String[] splitLine =  line.split("x", 2);
					recipes = extend(recipes);
					recipes[recipes.length - 1] = new Recipe(splitLine[1].trim(), Integer.parseInt(splitLine[0]));
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Something went wrong while reading line: " + line);
			return null;
		}
		return recipes;
	}

	private static Recipe[] extend(Recipe[] oldArray)
	{
		Recipe[] newArray = new Recipe[oldArray.length + 1];
		for(int i = 0; i < oldArray.length; i++)
		{
			newArray[i] = oldArray[i];
		}
		return newArray;
	}

	private static String[] extend(String[] oldArray)
	{
		String[] newArray = new String[oldArray.length + 1];
		for(int i = 0; i < oldArray.length; i++)
		{
			newArray[i] = oldArray[i];
		}
		return newArray;
	}

	public static String arrayString(String[] array)
	{
		String result = "[";

		if(array.length > 0)
			result += array[0];

		for(int i = 1; i < array.length; i++)
			result += ", " + array[i];

		result += "]";

		return result;
	}

	public static void connect(Recipe[] recipes)
	{
		for(int i = 0; i < recipes.length; i++)
		{
			String[] ingredientNames = recipes[i].ingredientNames();

			for(int j = 0; j < ingredientNames.length; j++)
				for(int k = 0; k < recipes.length; k++)
					if(recipes[k].name().equals(ingredientNames[j]))
						recipes[i].connect(ingredientNames[j], recipes[k]);
		}
	}


	public static void connect(Recipe recipe, Recipe[] recipes)
	{
			String[] ingredientNames = recipe.ingredientNames();

			for(int i = 0; i < ingredientNames.length; i++)
				for(int j = 0; j < recipes.length; j++)
					if(recipes[j].name().equals(ingredientNames[i]))
						recipe.connect(ingredientNames[i], recipes[j]);
	}


	public static boolean loopy(Recipe[] recipes)
	{
		int[] inDegree = getInDegree(recipes);

		inDegree = reduceInDegree(recipes, inDegree);

		for(int i = 0; i < inDegree.length; i++)
			if(inDegree[i] > 0)
				return true;

		return false;
	}


	public static String[] loop(Recipe[] recipes)
	{
		int[] inDegree = getInDegree(recipes);

		inDegree = reduceInDegree(recipes, inDegree);

		int next = 0;
		for(int i = 0; i < inDegree.length; i++)
			if(inDegree[i] > 0)
				next = i;

		String[] search = new String[0];
		int loopIndex = 0;
		boolean copleteLoop = false;
		while(!copleteLoop)
		{
			search = extend(search);
			search[search.length - 1] = recipes[next].name();

			String[] ingredientNames = recipes[next].ingredientNames();
			for(int i = 0; i < ingredientNames.length; i++)
				for(int j = 0; j < recipes.length; j++)
					if(recipes[j].name().equals(ingredientNames[i]))
						if(inDegree[j] > 0)
							next = j;

			for(int i = 0; i < search.length; i++)
				if(recipes[next].name().equals(search[i]))
				{
					search = extend(search);
					search[search.length - 1] = recipes[next].name();
					loopIndex = i;
					copleteLoop = true;
					break;
				}
		}

		String[] result = new String[search.length - loopIndex];

		for(int i = 0; i < result.length; i++)
			result[i] = search[i + loopIndex];

		return result;
	}

	public static int[] getInDegree(Recipe[] recipes)
	{
		int[] result = new int[recipes.length];
		for(int i = 0; i < recipes.length; i++)
		{
			result[i] = 0;
			for(int j = 0; j < recipes.length; j++)
			{
				String[] ingredientNames = recipes[j].ingredientNames();
				for(int k = 0; k < ingredientNames.length; k++)
					if(recipes[i].name().equals(ingredientNames[k]))
						result[i]++;
			}
		}
		return result;
	}

	public static int[] reduceInDegree(Recipe[] recipes, int[] inDegree)
	{
		int[] reduce = inDegree.clone();

		boolean zeroFound;
		int zeroIndex;

		do
		{
			zeroFound = false;
			for(int i = 0; i < reduce.length; i++)
			{
				if(reduce[i] == 0)
				{
					zeroFound = true;
					zeroIndex = i;

					String[] ingredientNames = recipes[zeroIndex].ingredientNames();

					for(int j = 0; j < ingredientNames.length; j++)
						for(int k = 0; k < recipes.length; k++)
							if(recipes[k].name().equals(ingredientNames[j]))
								reduce[k] --;

					reduce[i] --;
					break;
				}
			}
		}
		while(zeroFound);

		for(int i = 0; i < reduce.length; i++)
			if(reduce[i] < 0)
				reduce[i] = 0;

		return reduce;
	}

}



class Recipe
{
	private String name;
	private int quantity;
	private String description;
	private int time;
	private String[] ingredientNames;
	private int[] ingredientQuantities;
	private Recipe[] ingredientLinks;

	Recipe(String name, int quantity)
	{
		this.name = name;
		this.quantity = quantity;
		description = "";
		time = 0;
		ingredientNames = new String[0];
		ingredientQuantities = new int[0];
		ingredientLinks = new Recipe[0];
	}

	public void time(int time)
	{
		this.time = time;
	}

	public void description(String description)
	{
		if(this.description.equals(""))
		{
			this.description += description;
		}
		else
		{
			this.description += "\n" + description;
		}
	}

	public String name()
	{
		return name;
	}

	public int quantity()
	{
		return quantity;
	}

	public int time()
	{
		return time;
	}

	public String description()
	{
		return description;
	}

	public String[] ingredientNames()
	{
		return ingredientNames.clone();
	}

	public Recipe[] ingredientLinks()
	{
		return ingredientLinks.clone();
	}

	public void ingredient(String name, int quantity)
	{
		int i;

		String[] newNames = new String[ingredientNames.length + 1];
		for(i = 0; i < ingredientNames.length; i++)
		{
			newNames[i] = ingredientNames[i];
		}
		newNames[i] = name;
		ingredientNames = newNames;

		int[] newQuantities = new int[ingredientQuantities.length + 1];
		for(i = 0; i < ingredientQuantities.length; i++)
		{
			newQuantities[i] = ingredientQuantities[i];
		}
		newQuantities[i] = quantity;
		ingredientQuantities = newQuantities;

		Recipe[] newLinks = new Recipe[ingredientLinks.length + 1];
		for(i = 0; i < ingredientLinks.length; i++)
		{
			newLinks[i] = ingredientLinks[i];
		}
		newLinks[i] = null;
		ingredientLinks = newLinks;
	}

	public void connect(String ingredientName, Recipe ingredientLink)
	{
		for(int i = 0; i < ingredientNames.length; i++)
		{
			if(ingredientNames[i].equals(ingredientName))
				ingredientLinks[i] = ingredientLink;
		}
	}

	public String creationCode()
	{
		String result = Integer.toString(quantity) + "x " + name + "\n";

		if(time >= 0)
			result += "$time = " + Integer.toString(time) + "\n";

		if(!description.isEmpty())
			result += "* " + description + "\n";

		for(int i = 0; i < ingredientNames.length; i++)
			result += "> " + Integer.toString(ingredientQuantities[i]) + "x "
				+ ingredientNames[i] + "\n";

		return result;
	}

	public boolean baseMaterial()
	{
		if(ingredientNames.length == 0)
			return true;
		else
			return false;
	}

	public void craft(int required, CraftingList list, CraftingList leftovers)
	{
		int needed;
		if(leftovers.find(name, this) < 0)
			needed = required;
		else
			needed = required - leftovers.quantities()[leftovers.find(name, this)];

		int craftCycles = (int)Math.ceil(needed/(double)quantity);
		int toCraft = this.quantity*craftCycles;

		for(int i = 0; i < ingredientLinks.length; i++)
			if(ingredientLinks[i] == null)
				list.add(ingredientNames[i], ingredientQuantities[i]*craftCycles);
			else
				ingredientLinks[i].craft(ingredientQuantities[i]*craftCycles, list, leftovers);

		list.add(this, toCraft);
		leftovers.add(this, toCraft - required);
	}

	public String toString()
	{
		CraftingList list = new CraftingList();
		CraftingList leftovers = new CraftingList();
		craft(1, list, leftovers);

		String[] listNames = list.names();
		Recipe[] listIngredients = list.ingredients();
		int[] listQuantities = list.quantities();

		String[] leftoversNames = leftovers.names();
		Recipe[] leftoversIngredients = leftovers.ingredients();
		int[] leftoversQuantities = leftovers.quantities();

		int estimatedTime = 0;
		String result = "\tBase Materials:\n";
		int i;
		for(i = 0; i < listNames.length - 1; i++)
		{
			boolean primary;
			if(listIngredients[i] == null)
				primary = true;
			else if(listIngredients[i].baseMaterial())
				primary = true;
			else
				primary = false;

			if(primary)
			{
				result += "\t\t" + Integer.toString(listQuantities[i]) + "x " + listNames[i];
				if(listIngredients[i] != null)
				{
					if(!listIngredients[i].description().trim().equals(""))
						if(listIngredients[i].description().split("\n").length == 1)
							result += " (" + listIngredients[i].description() + ")";
						else for(int j = 0; j < listIngredients[i].description().split("\n").length; j++)
							result += "\n\t\t\t" + listIngredients[i].description().split("\n")[j];
					estimatedTime += listIngredients[i].time()*listQuantities[i]/(double)listIngredients[i].quantity();
				}
				result += "\n";
			}
		}

		result += "\n\tTo Craft (In craftable order, not necessarily recommended order):\n";

		for(i = 0; i < listNames.length - 1; i++)
		{
			boolean primary;
			if(listIngredients[i] == null)
				primary = true;
			else if(listIngredients[i].baseMaterial())
				primary = true;
			else
				primary = false;

			if(!primary)
			{
				result += "\t\t" + Integer.toString(listQuantities[i]) + "x " + listNames[i];
				if(listIngredients[i] != null)
				{
					if(!listIngredients[i].description().trim().equals(""))
						if(listIngredients[i].description().split("\n").length == 1)
							result += " (" + listIngredients[i].description() + ")";
						else for(int j = 0; j < listIngredients[i].description().split("\n").length; j++)
							result += "\n\t\t\t" + listIngredients[i].description().split("\n")[j];
					estimatedTime += listIngredients[i].time()*listQuantities[i]/(double)listIngredients[i].quantity();
				}
				result += "\n";
			}
		}

		int leftoverCount = 0;
		for(int j = 0; j < leftoversQuantities.length; j++)
			if(leftoversQuantities[j] > 0)
				leftoverCount++;

		if(leftoverCount > 0)
			result += "\nleftovers:\n";

		for(int j = 0; j < leftoversNames.length - 1; j++)
		{
			if(leftoversQuantities[j] > 0)
				result += "\t\t" + Integer.toString(leftoversQuantities[j]) + "x " + leftoversNames[j] + "\n";
		}

		result = listNames[i] + ":\n" + result;

		result += "\nEstimated time: " + Integer.toString(estimatedTime);
		return result;
	}
}

class CraftingList
{
	private Recipe[] ingredients;
	private String[] names;
	private int[] quantities;

	CraftingList()
	{
		ingredients = new Recipe[0];
		names = new String[0];
		quantities = new int[0];
	}

	public void add(Recipe ingredient, int quantity)
	{
		int index = find(ingredient.name(), ingredient);
		if(index >= 0)
			quantities[index] += quantity;
		else
		{
			addEntry(ingredient.name(), ingredient);
			add(ingredient, quantity);
		}
	}

	public void add(String name, int quantity)
	{
		int index = find(name, null);
		if(index >= 0)
			quantities[index] += quantity;
		else
		{
			addEntry(name, null);
			add(name, quantity);
		}
	}

	public void addEntry(String name, Recipe ingredient)
	{
		Recipe[] newIngredients = new Recipe[ingredients.length + 1];
		for(int i = 0; i < ingredients.length; i++)
			newIngredients[i] = ingredients[i];
		newIngredients[ingredients.length] = ingredient;
		ingredients = newIngredients;

		String[] newNames = new String[names.length + 1];
		for(int i = 0; i < names.length; i++)
			newNames[i] = names[i];
		newNames[names.length] = name;
		names = newNames;

		int[] newQuantities = new int[quantities.length + 1];
		for(int i = 0; i < quantities.length; i++)
			newQuantities[i] = quantities[i];
		newQuantities[quantities.length] = 0;
		quantities = newQuantities;
	}

	public int find(String name, Recipe ingredient)
	{
		for(int i = 0; i < ingredients.length; i++)
			if((ingredients[i] == ingredient) && (names[i].equals(name)))
				return i;
		return -1;
	}

	public Recipe[] ingredients()
	{
		return ingredients.clone();
	}

	public String[] names()
	{
		return names.clone();
	}

	public int[] quantities()
	{
		return quantities.clone();
	}
}
