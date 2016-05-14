
package android.databinding;
import com.quickblox.sample.user.BR;
class DataBinderMapper {
    final static int TARGET_MIN_SDK = 14;
    public DataBinderMapper() {
    }
    public android.databinding.ViewDataBinding getDataBinder(android.databinding.DataBindingComponent bindingComponent, android.view.View view, int layoutId) {
        switch(layoutId) {
                case com.quickblox.sample.user.R.layout.activity_show_user:
                    return com.quickblox.sample.user.databinding.ActivityShowUserBinding.bind(view, bindingComponent);
                case com.quickblox.sample.user.R.layout.list_item_user:
                    return com.quickblox.sample.user.databinding.ListItemUserBinding.bind(view, bindingComponent);
        }
        return null;
    }
    android.databinding.ViewDataBinding getDataBinder(android.databinding.DataBindingComponent bindingComponent, android.view.View[] views, int layoutId) {
        switch(layoutId) {
        }
        return null;
    }
    int getLayoutId(String tag) {
        if (tag == null) {
            return 0;
        }
        final int code = tag.hashCode();
        switch(code) {
            case 712783059: {
                if(tag.equals("layout/activity_show_user_0")) {
                    return com.quickblox.sample.user.R.layout.activity_show_user;
                }
                break;
            }
            case -549586548: {
                if(tag.equals("layout/list_item_user_0")) {
                    return com.quickblox.sample.user.R.layout.list_item_user;
                }
                break;
            }
        }
        return 0;
    }
    String convertBrIdToString(int id) {
        if (id < 0 || id >= InnerBrLookup.sKeys.length) {
            return null;
        }
        return InnerBrLookup.sKeys[id];
    }
    private static class InnerBrLookup {
        static String[] sKeys = new String[]{
            "_all"
            ,"user"};
    }
}