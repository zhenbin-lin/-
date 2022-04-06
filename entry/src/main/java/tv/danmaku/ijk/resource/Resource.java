package tv.danmaku.ijk.resource;

import tv.danmaku.ijk.constant.MediaConstant;

public class Resource implements IResource {
    public Resource(String uri){
        this.uri = uri;
    }

    public String getResourcePath() {
        if(uri == null || !uri.startsWith(MediaConstant.OHOS_RESOURCE_SCHEME)) {
            return "";
        }
        return uri.substring(MediaConstant.OHOS_RESOURCE_SCHEME.length());
    }

    private String uri;
}
