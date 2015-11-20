package in.newzup;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class FooterListViewFromXML extends ListView {
    private int footerId;

    public FooterListViewFromXML(Context context) {
        this(context, null);
    }

    public FooterListViewFromXML(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FooterListViewFromXML(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FooterListViewFromXML, defStyle, defStyle);

        try {
            footerId = a.getResourceId(R.styleable.FooterListViewFromXML_footerView, View.NO_ID);
            if (footerId != View.NO_ID) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View header = inflater.inflate(footerId, null);
                addFooterView(header);
            }
        } finally {
            a.recycle();
        }
    }
}