package online.sniper.utils.sdcard;

import android.os.StatFs;
import android.text.TextUtils;

/**
 * SD卡基本信息
 *
 * @author wangpeihe
 */
public class SDCard {
    public String name;
    public String rootPath;
    public String writePath;
    /**
     * Android4.4增加了SD卡读写权限设置，分为内置存储和外置SD卡，对权限见下表：<br>
     * <table width="60%" border="1" align="center">
     * <tr>
     * <th align="center">Action</th>
     * <th align="center">Primary</th>
     * <th align="center">Secondary</th>
     * </tr>
     * <tbody>
     * <tr>
     * <td>Read Top-Level Directories</td>
     * <td align="center">R</td>
     * <td align="center">R</td>
     * </tr>
     * <tr>
     * <td>Write Top-Level Directories</td>
     * <td align="center">W</td>
     * <td align="center">N</td>
     * </tr>
     * <tr>
     * <td>Read My Package&#8217;s Android Data Directory</td>
     * <td align="center">Y</td>
     * <td align="center">Y</td>
     * </tr>
     * <tr>
     * <td>Write My Package&#8217;s Android Data Directory</td>
     * <td align="center">Y</td>
     * <td align="center">Y</td>
     * </tr>
     * <tr>
     * <td>Read Another Package&#8217;s Android Data Directory</td>
     * <td align="center">R</td>
     * <td align="center">R</td>
     * </tr>
     * <tr>
     * <td>Write Another Package&#8217;s Android Data Directory</td>
     * <td align="center">W</td>
     * <td align="center">N</td>
     * </tr>
     * </tbody>
     * </table>
     * <p style="text-align: center;"><strong>R = With Read Permission, W = With Write Permission, Y = Always, N = Never
     * </strong></p>
     * 根据上面表格判断SD类型，这个属性代表了Write Top-Level Directories的Secondary(外置SD卡).<br>
     * 由于部分手机厂商没有遵循Google新的SD卡规范，所以在部分Android4.4手机上外置SD卡的根目录仍然有读写
     * 权限.所以只有在Android4.4以上手机，并且外置SD卡不可写的情况此属性才为<strong>false</strong>.
     */
    public boolean canWriteToRootPath = true;

    public SDCard(String path) {
        check(path);
        this.rootPath = path;
    }

    private void check(String path) {
        new StatFs(path);
    }

    public boolean hasWritablePath() {
        return !TextUtils.isEmpty(writePath);
    }

}
