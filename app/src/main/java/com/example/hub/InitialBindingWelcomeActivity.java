/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub
 * @ClassName: InitialBindingWelcomeActivity
 * @Description: Welcome page of the Hub
 * @Author: GRP_Team14
 * @CreateDate: 2022/3/17
 * @Version: 1.0
 */

package com.example.hub;

import androidx.fragment.app.Fragment;

import com.example.hub.Fragment.CustomizedFragment;
import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.FragmentWelcomePage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;
import com.stephentuso.welcome.WelcomeHelper;

public class InitialBindingWelcomeActivity extends WelcomeActivity {

    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultTitleTypefacePath("Montserrat-Bold.ttf")
                .defaultHeaderTypefacePath("Montserrat-Bold.ttf")

                .page(new BasicPage(R.drawable.ic_edge_hub_connection,
                        "Welcome",
                        "Please follow the instruction to pair sensor with the Edge Computer.")
                        .background(R.color.orange_background)
                )

                .page(new BasicPage(R.drawable.ic_wifi,
                        "First thing first",
                        "Switch on your Wi-Fi to enable the binding process with the Edge, check the connectivity on Edge Computer too.")
                        .background(R.color.green_background)
                )

                .page(new BasicPage(R.drawable.ic_ipaddress,
                        "Find your IP address",
                        "Go to the Hub settings and follow the steps: \nSettings - About phone - Status - IP address")
                        .background(R.color.teal_background)
                )

                .page(new BasicPage(R.drawable.ic_manuallyinput,
                        "Manually IP configuration",
                        "Go to the Edge Preference and manually input your IP , do not change the default port.")
                        .background(R.color.blue_background)
                )

                .page(new FragmentWelcomePage() {
                            @Override
                            protected Fragment fragment() {
                                return new CustomizedFragment();
                            }
                        }
                        .background(R.color.blue_purple_background)
                )
                .swipeToDismiss(false) // when entering the last page, it cannot proceed unless pairing is successful
                .useCustomDoneButton(true)
                .canSkip(false)
                .exitAnimation(android.R.anim.fade_out)
                .build();
    }
}
