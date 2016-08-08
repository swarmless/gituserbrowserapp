package swarmless.gituserbrowserapp.adapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;



/**
 * Created by Firas-PC on 06.08.2016.
 *
 *
 */

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    //public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean isLoading = true; // True if we are still waiting for the last set of data to load.
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private LinearLayoutManager mLinearLayoutManager;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();




        if (isLoading) {
            if (totalItemCount > previousTotal) { // previousTotal must be reset on refresh
                isLoading = false;
                previousTotal = totalItemCount;
            }
        }
        int visibleThreshold = 3;
        if (!isLoading && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached

            onLoadMore();
            isLoading = true;

        }
    }

    public abstract void onLoadMore();

}