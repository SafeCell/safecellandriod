package com.safecell;

import java.util.ArrayList;

import android.util.Log;

public class QueueSong {

		public ArrayList<Integer> list;
		
		
		// Queue constructor
		public QueueSong()
		{
			// Create a new LinkedList.
			list = new ArrayList<Integer>();
			
		}

		public boolean isEmpty(){
			return (list.size()==0);
		}

		public  void enqueue(int item)	
		{
			if (existInQueue(item)) {
				//Log.v("Safecell :"+"Sound Exists", list.size()+"");
				return;
			}
			list.add(item);
			//Log.v("Safecell :"+"SIZE of play file", list.size()+"");
		}

		public synchronized int dequeue(){
			
			if(list.size() == 0) {
				return -1;
			}
			
			int item = list.remove(0);
			//Log.v("Safecell :"+"SIZE of dequeue play file", list.size()+"");
			return item;
		}

		public int peek(){
			
			
			return  list.get(0);
		}
	
		public boolean existInQueue(int item){
			
			return list.contains(item);
			
		}
}
