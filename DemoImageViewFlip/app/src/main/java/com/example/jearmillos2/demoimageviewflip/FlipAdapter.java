package com.example.jearmillos2.demoimageviewflip;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FlipAdapter extends BaseAdapter implements OnClickListener {


    //For opening current page, render it, and close the page
    private PdfRenderer.Page mCurrentPage;
    //For rendering a PDF document
    private PdfRenderer mPdfRenderer;
    public interface Callback{
        public void onPageRequested(int page);
    }

    static class Item {
        static long id = 0;

        long mId;

        public Item() {
            mId = id++;
        }

        long getId(){
            return mId;
        }
    }

    private LayoutInflater inflater;
    private Callback callback;
    private List<Item> items = new ArrayList<Item>();

    public FlipAdapter(Context context,PdfRenderer mPdfRenderer) {
        inflater = LayoutInflater.from(context);
        this.mPdfRenderer = mPdfRenderer;
        for(int i = 0 ; i<mPdfRenderer.getPageCount(); i++){
            items.add(new Item());
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.page, parent, false);

            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.firstPage = (Button) convertView.findViewById(R.id.first_page);
            holder.lastPage = (Button) convertView.findViewById(R.id.last_page);
            holder.imagen = (ImageView) convertView.findViewById(R.id.imageView);
            holder.firstPage.setOnClickListener(this);
            holder.lastPage.setOnClickListener(this);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        //TODO set a text with the id as well
        //holder.text.setText(items.get(position).getId()+":"+position);
        holder.text.setText("");
        /*if(position % 2 == 0){
            holder.imagen.setImageResource(R.drawable.ic_launcher);
        }else{
            holder.imagen.setImageResource(R.mipmap.ic_launcher);
        }*/
        holder.imagen.setImageBitmap(showPage(position));

        return convertView;
    }

    static class ViewHolder{
        TextView text;
        Button firstPage;
        Button lastPage;
        ImageView imagen;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.first_page:
                if(callback != null){
                    callback.onPageRequested(0);
                }
                break;
            case R.id.last_page:
                if(callback != null){
                    callback.onPageRequested(getCount()-1);
                }
                break;
        }
    }

    public void addItems(int amount) {
        for(int i = 0 ; i<amount ; i++){
            items.add(new Item());
        }
        notifyDataSetChanged();
    }

    public void addItemsBefore(int amount) {
        for(int i = 0 ; i<amount ; i++){
            items.add(0, new Item());
        }
        notifyDataSetChanged();
    }

    /**
     * API show to particular page index using PdfRenderer
     * @param index
     */
    private Bitmap showPage(int index) {
        if (mPdfRenderer == null || mPdfRenderer.getPageCount() <= index
                || index < 0) {
            return null;
        }
        // For closing the current page before opening another one.
        try {
            if (mCurrentPage != null) {
                mCurrentPage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Open page with specified index
        mCurrentPage = mPdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(),
                mCurrentPage.getHeight(), Bitmap.Config.ARGB_8888);

        //Pdf page is rendered on Bitmap
        mCurrentPage.render(bitmap, null, null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        //Set rendered bitmap to ImageView
        return bitmap;
    }

}
