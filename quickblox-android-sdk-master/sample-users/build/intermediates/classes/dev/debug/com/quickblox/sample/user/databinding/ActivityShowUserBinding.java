package com.quickblox.sample.user.databinding;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.BR;
import android.view.View;
public class ActivityShowUserBinding extends android.databinding.ViewDataBinding  {

    private static final android.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.activity_show_user, 6);
    }
    // views
    public final android.widget.ScrollView activityShowUser;
    public final android.widget.EditText emailTextview;
    public final android.widget.EditText fullNameTextview;
    public final android.widget.EditText loginTextview;
    private final android.support.design.widget.CoordinatorLayout mboundView0;
    public final android.widget.EditText phoneTextview;
    public final android.widget.EditText tagTextview;
    // variables
    private com.quickblox.users.model.QBUser mUser;
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityShowUserBinding(android.databinding.DataBindingComponent bindingComponent, View root) {
        super(bindingComponent, root, 0);
        final Object[] bindings = mapBindings(bindingComponent, root, 7, sIncludes, sViewsWithIds);
        this.activityShowUser = (android.widget.ScrollView) bindings[6];
        this.emailTextview = (android.widget.EditText) bindings[3];
        this.emailTextview.setTag(null);
        this.fullNameTextview = (android.widget.EditText) bindings[2];
        this.fullNameTextview.setTag(null);
        this.loginTextview = (android.widget.EditText) bindings[1];
        this.loginTextview.setTag(null);
        this.mboundView0 = (android.support.design.widget.CoordinatorLayout) bindings[0];
        this.mboundView0.setTag(null);
        this.phoneTextview = (android.widget.EditText) bindings[4];
        this.phoneTextview.setTag(null);
        this.tagTextview = (android.widget.EditText) bindings[5];
        this.tagTextview.setTag(null);
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
        java.lang.String phoneUser = null;
        java.lang.String fullNameUser = null;
        com.quickblox.core.helper.StringifyArrayList<java.lang.String> tagsUser = null;
        java.lang.String loginUser = null;
        java.lang.String comQuickbloxSampleUs = null;
        com.quickblox.users.model.QBUser user = mUser;
        java.lang.String emailUser = null;

        if ((dirtyFlags & 0x3L) != 0) {



                if (user != null) {
                    // read user.phone
                    phoneUser = user.getPhone();
                    // read user.fullName
                    fullNameUser = user.getFullName();
                    // read user.tags
                    tagsUser = user.getTags();
                    // read user.login
                    loginUser = user.getLogin();
                    // read user.email
                    emailUser = user.getEmail();
                }


                // read com.quickblox.sample.user.helper.Utils.ListToString(user.tags)
                comQuickbloxSampleUs = com.quickblox.sample.user.helper.Utils.ListToString(tagsUser);
        }
        // batch finished
        if ((dirtyFlags & 0x3L) != 0) {
            // api target 1

            android.databinding.adapters.TextViewBindingAdapter.setText(this.emailTextview, emailUser);
            android.databinding.adapters.TextViewBindingAdapter.setText(this.fullNameTextview, fullNameUser);
            android.databinding.adapters.TextViewBindingAdapter.setText(this.loginTextview, loginUser);
            android.databinding.adapters.TextViewBindingAdapter.setText(this.phoneTextview, phoneUser);
            android.databinding.adapters.TextViewBindingAdapter.setText(this.tagTextview, comQuickbloxSampleUs);
        }
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;

    public static ActivityShowUserBinding inflate(android.view.LayoutInflater inflater, android.view.ViewGroup root, boolean attachToRoot) {
        return inflate(inflater, root, attachToRoot, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static ActivityShowUserBinding inflate(android.view.LayoutInflater inflater, android.view.ViewGroup root, boolean attachToRoot, android.databinding.DataBindingComponent bindingComponent) {
        return android.databinding.DataBindingUtil.<ActivityShowUserBinding>inflate(inflater, com.quickblox.sample.user.R.layout.activity_show_user, root, attachToRoot, bindingComponent);
    }
    public static ActivityShowUserBinding inflate(android.view.LayoutInflater inflater) {
        return inflate(inflater, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static ActivityShowUserBinding inflate(android.view.LayoutInflater inflater, android.databinding.DataBindingComponent bindingComponent) {
        return bind(inflater.inflate(com.quickblox.sample.user.R.layout.activity_show_user, null, false), bindingComponent);
    }
    public static ActivityShowUserBinding bind(android.view.View view) {
        return bind(view, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static ActivityShowUserBinding bind(android.view.View view, android.databinding.DataBindingComponent bindingComponent) {
        if (!"layout/activity_show_user_0".equals(view.getTag())) {
            throw new RuntimeException("view tag isn't correct on view:" + view.getTag());
        }
        return new ActivityShowUserBinding(bindingComponent, view);
    }
    /* flag mapping
        flag 0 (0x1L): user
        flag 1 (0x2L): null
    flag mapping end*/
    //end
}