package swarmless.gituserbrowserapp.adapters;

import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.widget.RecyclerView;

import android.view.ViewGroup;

import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;


import java.util.List;

import swarmless.gituserbrowserapp.R;
import swarmless.gituserbrowserapp.activites.MainActivity;
import swarmless.gituserbrowserapp.models.User;


/**
 * Created by Firas-PC on 06.08.2016.
 *
 *
 */
public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int TYPE_FOOTER = 0;
    private static final int TYPE_ITEM = 1;

    protected MainActivity mainActivity;
    protected List<User> dataset;


    public UserAdapter(MainActivity mainActivity, List<User> dataset) {
        this.mainActivity = mainActivity;
        this.dataset = dataset;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout main;
        public ImageView avatar;
        public ProgressBar progressBar;
        public TextView name;

        public ViewHolder(View v) {

            super(v);

            main = (LinearLayout) v.findViewById(R.id.single_user_item_view_main);
            avatar = (ImageView) v.findViewById(R.id.single_user_item_view_avatar);
            name = (TextView) v.findViewById(R.id.single_user_item_view_name);
            progressBar = (ProgressBar) v.findViewById(R.id.single_user_item_view_avatar_progress);

        }


    }

    public static class ViewHolderFooter extends RecyclerView.ViewHolder {


        public ViewHolderFooter(View v) {
            super(v);


        }


    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflates the view
        if (viewType == TYPE_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_item_view_footer, parent, false);
            return new ViewHolderFooter(v);
        } else if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_item_view, parent, false);
            return new ViewHolder(v);
        } else return null;

    }


    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {


        // set the users avatar and round it
        if (holder instanceof ViewHolder) {
            final ViewHolder castHolder = (ViewHolder) holder;

            if (dataset.get(position).getAvatar_url() != null || !dataset.get(position).getAvatar_url().equals("") || URLUtil.isValidUrl(dataset.get(position).getAvatar_url())) {

                Glide.with(mainActivity)
                        .load(dataset.get(position).getAvatar_url())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.mipmap.ic_avatar_placeholder)
                        .error(R.drawable.default_avatar)
                        .into(new BitmapImageViewTarget(castHolder.avatar) {

                            @Override
                            protected void setResource(Bitmap resource) { // rounding the avatar
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(mainActivity.getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                castHolder.avatar.setImageDrawable(circularBitmapDrawable);
                            }
                        });
            } else {
                // make sure Glide doesn't load anything into this view until told otherwise
                Glide.clear(castHolder.avatar);

                // load the default avatar @drawable/default_avatar ..
                castHolder.avatar.setImageResource(R.drawable.default_avatar);
            }

            // set the user's login
            if (dataset.get(position).getLogin() != null || !dataset.get(position).getLogin().equals("")) { // if null or empty - "no name" will be written

                castHolder.name.setText(dataset.get(position).getLogin());
            }

            castHolder.main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // open users page
                    if (dataset.get(position).getHtml_url() != null || !dataset.get(position).getHtml_url().equals("") || URLUtil.isValidUrl(dataset.get(position).getHtml_url())) {

                        mainActivity.openUserPageInBrowser(dataset.get(position).getHtml_url());

                    } else {// if null or empty string or even broken a dialog will be showed

                        mainActivity.showUserPageUrlIsNotValid();

                    }


                }
            });
        }


    }

    @Override
    public int getItemViewType(int position) {
        return dataset.get(position) == null ? TYPE_FOOTER : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return dataset == null ? 0 : dataset.size();
    }

    public int getLastId() {

        return getItemCount() == 0 ? 0 : dataset.get(getItemCount() - 2).getId();
    }

    public void clear() {
        dataset.clear();
        notifyDataSetChanged();
    }


    public void addAll(List<User> list) {
        int oldSize = dataset.size();
        dataset.addAll(list);
        notifyItemRangeInserted(oldSize, dataset.size() - 1);

    }

    public void addItem(User user) {

        dataset.add(user);
        this.notifyItemInserted(dataset.indexOf(user));
    }

    public void removerItem(int position) {
        dataset.remove(position);
        notifyItemRemoved(position);

    }

    public boolean isEmpty(){
        return dataset.isEmpty();
    }

}
