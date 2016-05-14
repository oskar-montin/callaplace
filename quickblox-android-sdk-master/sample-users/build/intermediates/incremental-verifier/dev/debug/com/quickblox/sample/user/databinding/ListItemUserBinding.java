package com.quickblox.sample.user.databinding;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.BR;
import android.view.View;
public class ListItemUserBinding extends android.databinding.ViewDataBinding  {

    private static final android.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.user_avatar2_imageview, 3);
        sViewsWithIds.put(R.id.user_avatar_imageview, 4);
    }
    // views
    public final android.widget.TextView fullNameTextItemView;
    private final android.widget.RelativeLayout mboundView0;
    public final android.widget.ImageView userAvatar2Imageview;
    public final android.widget.ImageView userAvatarImageview;
    public final com.devspark.robototextview.widget.RobotoTextView userNameTextItemView;
    // variables
    private com.quickblox.users.model.QBUser mUser;
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ListItemUserBinding(android.databinding.DataBindingComponent bindingComponent, View root) {
        super(bindingComponent, root, 0);
        final Object[] bindings = mapBindings(bindingComponent, root, 5, sIncludes, sViewsWithIds);
        this.fullNameTextItemView = (android.widget.TextView) bindings[2];
        this.fullNameTextItemView.setTag(null);
        this.mboundView0 = (android.widget.RelativeLayout) bindings[0];
        this.mboundView0.setTag(null);
        this.userAvatar2Imageview = (android.widget.ImageView) bindings[3];
        this.userAvatarImageview = (android.widget.ImageView) bindings[4];
        this.userNameTextItemView = (com.devspark.robototextview.widget.RobotoTextView) bindings[1];
        this.userNameTextItemView.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x2L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean setVariable(int variableId, Object variable) {
        switch(variableId) {
            case BR.user :
                setUser((com.quickblox.users.model.QBUser) variable);
                return true;
        }
        return false;
    }

    public void setUser(com.quickblox.users.model.QBUser user) {
        this.mUser = user;
        synchronized(this) {
            mDirtyFlags |= 0x1L;
        }
        notifyPropertyChanged(BR.user);
        super.requestRebind();
    }
    public com.quickblox.users.model.QBUser getUser() {
        return mUser;
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        java.lang.String fullNameUser = null;
        java.lang.String loginUser = null;
        com.quickblox.users.model.QBUser user = mUser;

        if ((dirtyFlags & 0x3L) != 0) {



                if (user != null) {
                    // read user.fullName
                    fullNameUser = user.getFullName();
                    // read user.login
                    loginUser = user.getLogin();
                }
        }
        // batch finished
        if ((dirtyFlags & 0x3L) != 0) {
            // api target 1

            android.databinding.adapters.TextViewBindingAdapter.setText(this.fullNameTextItemView, fullNameUser);
            android.databinding.adapters.TextViewBindingAdapter.setText(this.userNameTextItemView, loginUser);
        }
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;

    public static ListItemUserBinding inflate(android.view.LayoutInflater inflater, android.view.ViewGroup root, boolean attachToRoot) {
        return inflate(inflater, root, attachToRoot, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static ListItemUserBinding inflate(android.view.LayoutInflater inflater, android.view.ViewGroup root, boolean attachToRoot, android.databinding.DataBindingComponent bindingComponent) {
        return android.databinding.DataBindingUtil.<ListItemUserBinding>inflate(inflater, com.quickblox.sample.user.R.layout.list_item_user, root, attachToRoot, bindingComponent);
    }
    public static ListItemUserBinding inflate(android.view.LayoutInflater inflater) {
        return inflate(inflater, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static ListItemUserBinding inflate(android.view.LayoutInflater inflater, android.databinding.DataBindingComponent bindingComponent) {
        return bind(inflater.inflate(com.quickblox.sample.user.R.layout.list_item_user, null, false), bindingComponent);
    }
    public static ListItemUserBinding bind(android.view.View view) {
        return bind(view, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static ListItemUserBinding bind(android.view.View view, android.databinding.DataBindingComponent bindingComponent) {
        if (!"layout/list_item_user_0".equals(view.getTag())) {
            throw new RuntimeException("view tag isn't correct on view:" + view.getTag());
        }
        return new ListItemUserBinding(bindingComponent, view);
    }
    /* flag mapping
        flag 0 (0x1L): user
        flag 1 (0x2L): null
    flag mapping end*/
    //end
}