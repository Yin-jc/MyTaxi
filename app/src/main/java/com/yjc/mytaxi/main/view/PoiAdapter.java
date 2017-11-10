package com.yjc.mytaxi.main.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yjc.mytaxi.R;

import java.util.List;

/**
 * Created by Administrator on 2017/11/9/009.
 */

public class PoiAdapter extends ArrayAdapter {
    private LayoutInflater inflater;
    private List<String> data;
    private OnItemClickListener mOnItemClickListener;

    public PoiAdapter(Context context,List data){
        super(context, R.layout.poi_list_item);
        this.data=data;
        inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mOnItemClickListener=listener;
    }

    public void setData(List<String> data){
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(data==null){
            return 0;
        }
        return data.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Holder holder=null;
        if(convertView==null){
            convertView=inflater.inflate(R.layout.poi_list_item,null);
            holder=new Holder();
            holder.textView=convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        }else {
            Object tag=convertView.getTag();
            if(tag==null){
                holder=new Holder();
                holder.textView=convertView.findViewById(R.id.name);
                convertView.setTag(holder);
            }else {
                holder= (Holder) tag;
            }
        }
        holder.id=position;
        holder.textView.setText(data.get(position));
        return convertView;
    }

    class Holder{
        int id;
        TextView textView;
    }

    public  interface  OnItemClickListener{
        void onItemClick(int id);
    }
}
