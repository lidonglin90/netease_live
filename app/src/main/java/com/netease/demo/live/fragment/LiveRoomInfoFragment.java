package com.netease.demo.live.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.demo.live.R;
import com.netease.demo.live.activity.LiveRoomActivity;
import com.netease.demo.live.base.BaseFragment;
import com.netease.demo.live.DemoCache;
import com.netease.demo.live.nim.adapter.MemberAdapter;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;

import java.util.List;

/**
 * Created by zhukkun on 1/9/17.
 */
public class LiveRoomInfoFragment extends BaseFragment {

    public static final String EXTRA_IS_AUDIENCE = "is_audence";

    public static LiveRoomInfoFragment getInstance(boolean isAudience){
        LiveRoomInfoFragment fragment = new LiveRoomInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_IS_AUDIENCE, isAudience);
        fragment.setArguments(bundle);
        return fragment;
    }

    boolean isAudience;
    TextView tvOnlineCount;
    TextView tvRoomName;
    TextView tvMasterName;

    RecyclerView recyclerView;
    MemberAdapter memberAdapter;

    int onlineCount;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        isAudience = getArguments().getBoolean(EXTRA_IS_AUDIENCE, true);
        return inflater.inflate(isAudience ? R.layout.layout_live_audience_room_info : R.layout.layout_live_captrue_room_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        tvOnlineCount = findView(R.id.online_count_text);
        tvRoomName = findView(R.id.room_name);
        recyclerView = findView(R.id.rv_room_member);
        initRecycleView();
        if(isAudience){
            tvMasterName = findView(R.id.master_name);
        }
    }

    private void initRecycleView() {
        memberAdapter = new MemberAdapter(getContext());
        recyclerView.setAdapter(memberAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        memberAdapter.setOnItemClickListener(new MemberAdapter.ItemClickListener() {
            @Override
            public void onItemClick(ChatRoomMember member) {
                ((LiveRoomActivity)getActivity()).onMemberOperate(member);
            }
        });
    }

    public void updateMember(List<ChatRoomMember> members){
        memberAdapter.updateMember(members);
        onlineCount = members.size();
        tvOnlineCount.setText(onlineCount + "人");
    }

    public void refreshRoomInfo(ChatRoomInfo roomInfo) {
        onlineCount = roomInfo.getOnlineUserCount();
        tvOnlineCount.setText(onlineCount+"人");
        tvRoomName.setText(roomInfo.getRoomId());
        if(isAudience) {
            tvMasterName.setText(roomInfo.getCreator());
        }
    }

    public void onReceivedNotification(ChatRoomNotificationAttachment attachment) {

        ChatRoomMember chatRoomMember = new ChatRoomMember();
        chatRoomMember.setAccount(attachment.getTargets().get(0));
        chatRoomMember.setNick(attachment.getTargetNicks().get(0));

        if(!isAudience && chatRoomMember.getAccount().equals(DemoCache.getAccount())){
            //主播的通知(主播进入房间,主播离开房间)不做处理,
            return;
        }

        switch (attachment.getType()) {
            case ChatRoomMemberIn:
                if(memberAdapter.addMember(chatRoomMember)) {
                    tvOnlineCount.setText(++onlineCount + "人");
                }
                break;
            case ChatRoomMemberExit:
            case ChatRoomMemberKicked:
                memberAdapter.removeMember(chatRoomMember);
                tvOnlineCount.setText(--onlineCount + "人");
                break;
            case ChatRoomMemberMuteAdd:
                chatRoomMember.setMuted(true);
                memberAdapter.updateSingleMember(chatRoomMember);
                break;
            case ChatRoomMemberMuteRemove:
                chatRoomMember.setMuted(false);
                memberAdapter.updateSingleMember(chatRoomMember);
                break;
        }
    }

    public void updateMemberState(ChatRoomMember current_operate_member) {
        memberAdapter.updateSingleMember(current_operate_member);
    }

    public ChatRoomMember getMember(String fromAccount) {
        return memberAdapter.getMember(fromAccount);
    }
}
