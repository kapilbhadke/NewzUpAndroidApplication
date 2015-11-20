package in.newzup.showcase;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;

import in.newzup.showcaseview.ShowcaseView;
import in.newzup.showcaseview.targets.ViewTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * This class allows you to have multiple showcases on one screen, you can then page through
 * each one as you press the 'ok' button
 */
public class ShowcaseViews {

    private final List<ShowcaseView> views = new ArrayList<ShowcaseView>();
    private final Activity activity;

    private interface OnShowcaseAcknowledged {
        void onShowCaseAcknowledged(ShowcaseView oldView);
    }

    /**
     * @param activity               The activity containing the views you wish to showcase
     * @param showcaseTemplateLayout Must be the layout of a ShowcaseView - use this to style your showcase
     */
    public ShowcaseViews(Activity activity, int showcaseTemplateLayout) {
        this.activity = activity;
    }

    public void addView(ViewProperties properties) {
        ShowcaseView viewTemplate = newInstanceOfShowcaseView();
        View v = activity.findViewById(properties.id);
        ViewTarget target = new ViewTarget(v);
        viewTemplate.setTarget(target);
        viewTemplate.setContentTitle(activity.getResources().getString(properties.titleResId));
        setChainClickListener(viewTemplate);
        views.add(viewTemplate);
    }
    
    public void addView(View destView, int titleResId) {
        ShowcaseView viewTemplate = newInstanceOfShowcaseView();
        ViewTarget target = new ViewTarget(destView);
        viewTemplate.setTarget(target);
        viewTemplate.setContentTitle(activity.getResources().getString(titleResId));
        setChainClickListener(viewTemplate);
        views.add(viewTemplate);
    }

    private ShowcaseView newInstanceOfShowcaseView() {
    	return new ShowcaseView(activity, true);
    }

    private void setChainClickListener(final ShowcaseView viewTemplate) {
        viewTemplate.overrideButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acknowledgedListener.onShowCaseAcknowledged(viewTemplate);
            }
        });
    }

    private OnShowcaseAcknowledged acknowledgedListener = new OnShowcaseAcknowledged() {
        @Override
        public void onShowCaseAcknowledged(ShowcaseView oldView) {
            oldView.hide();
            show();
        }
    };

    /**
     * Showcases will be shown in the order they where added, continuing when the button is pressed
     */
    public void show() {
        if (views.isEmpty()) {
        	SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
			editor.putInt("FIRST_HELP_SHOWN", 1);
			editor.apply();
            return;
        }
        final ShowcaseView view = views.get(0);
        ((ViewGroup) activity.getWindow().getDecorView()).addView(view);
        views.remove(0);
    }

    /**
     * Used for all views except those on the ActionBar
     */
    public static class ViewProperties {
        protected final int titleResId;
        protected final int id;

        public ViewProperties(int id, int titleResId) {
            this.id = id;
            this.titleResId = titleResId;
        }
    }

}
