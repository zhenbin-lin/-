import router from '@system.router';

export default {
    props: [
        'title',
        'action',
        "noIcon"
    ],
    data: {
        header: "",
        actionType: "",
        backSrc_value: "/common/image/back.png",
        moreSrc_value: "/common/image/more.png",
        hasMoreIcon: false,
        hasHeader: false
    },
    onInit() {
        this.header = this.title;
        this.hasHeader = !!this.header && !this.noIcon
        this.hasMoreIcon = this.action !== undefined
        this.actionType = this.action;
    },
    backImageClick() {
        router.back();
    },
    toSettingPage() {
        router.push({
            uri: 'pages/deviceSetting/deviceSetting'
        })
    }
}