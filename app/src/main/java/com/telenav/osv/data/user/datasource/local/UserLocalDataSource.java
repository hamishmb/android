package com.telenav.osv.data.user.datasource.local;

import androidx.annotation.NonNull;

import com.telenav.osv.application.ApplicationPreferences;
import com.telenav.osv.application.PreferenceTypes;
import com.telenav.osv.data.user.datasource.UserDataSource;
import com.telenav.osv.data.user.model.User;
import com.telenav.osv.utils.Log;
import com.telenav.osv.utils.StringUtils;

import io.reactivex.Completable;
import io.reactivex.Maybe;

/**
 * @author horatiuf
 */

public class UserLocalDataSource implements UserDataSource {

    /**
     * The {@code String} representing the TAG of the current class.
     */
    public static final String TAG = UserLocalDataSource.class.getSimpleName();

    /**
     * Empty value for int string.
     */
    private static final int UNKNOWN = -1;

    /**
     * Instance for the current class.
     */
    private static UserLocalDataSource INSTANCE;

    /**
     * Instance of {@code ApplicationPreferences} which persist user
     */
    private ApplicationPreferences appPrefs;

    /**
     * Default constructor for the current class. Private to prevent instantiation outside the class scope.
     * @param appPrefs the {@code ApplicationPreferences} used to instantiate the local persistence.
     */
    private UserLocalDataSource(@NonNull ApplicationPreferences appPrefs) {
        this.appPrefs = appPrefs;
    }

    /**
     * @param appPrefs the {@code ApplicationPreferences} used to instantiate the local persistence.
     * @return {@code UserLocalDataSource} representing {@link #INSTANCE}.
     */
    public static UserLocalDataSource getInstance(@NonNull ApplicationPreferences appPrefs) {
        if (INSTANCE == null) {
            INSTANCE = new UserLocalDataSource(appPrefs);
        }
        return INSTANCE;
    }

    @Override
    public Maybe<User> getUser() {
        return Maybe.create(emitter -> {
            String accessToken = appPrefs.getStringPreference(PreferenceTypes.K_ACCESS_TOKEN);
            String userId = appPrefs.getStringPreference(PreferenceTypes.K_USER_ID);
            String name = appPrefs.getStringPreference(PreferenceTypes.K_USER_NAME);
            String displayName = appPrefs.getStringPreference(PreferenceTypes.K_DISPLAY_NAME);
            int userType = appPrefs.getIntPreference(PreferenceTypes.K_USER_TYPE);
            String loginType = appPrefs.getStringPreference(PreferenceTypes.K_LOGIN_TYPE);
            int jarvisUserId = appPrefs.getIntPreference(PreferenceTypes.JARVIS_USER_ID);
            String jarvisUserName = appPrefs.getStringPreference(PreferenceTypes.JARVIS_USER_NAME);
            String jarvisAccessToken = appPrefs.getStringPreference(PreferenceTypes.JARVIS_ACCESS_TOKEN);
            String jarvisRefreshToken = appPrefs.getStringPreference(PreferenceTypes.JARVIS_REFRESH_TOKEN);

            if (StringUtils.isEmpty(userId)) {
                Log.d(TAG, String.format("getUser. Status: complete. Id: %s. Message: User not found.", userId));
                emitter.onComplete();
                return;
            }

            if (!validateUser(name, accessToken)) {
                String message = "User data not valid.";
                Log.d(TAG, String.format("getUser. Status: error. Id: %s. Message: %s.", userId, message));
                emitter.onError(new Throwable(message));
            } else {
                Log.d(TAG, String.format("getUser. Status: success. Id: %s. Message: User found.", userId));
                emitter.onSuccess(new User(
                        userId,
                        accessToken,
                        displayName,
                        loginType,
                        name,
                        userType,
                        jarvisUserId,
                        jarvisUserName,
                        jarvisAccessToken,
                        jarvisRefreshToken,
                        null));
            }
        });
    }

    @Override
    public Completable deleteUser() {
        return Completable.create(emitter -> {
            Log.d(TAG, "Delete user");
            saveUserData(
                    StringUtils.EMPTY_STRING,
                    StringUtils.EMPTY_STRING,
                    StringUtils.EMPTY_STRING,
                    StringUtils.EMPTY_STRING,
                    UNKNOWN,
                    StringUtils.EMPTY_STRING,
                    0,
                    StringUtils.EMPTY_STRING,
                    StringUtils.EMPTY_STRING,
                    StringUtils.EMPTY_STRING);
            emitter.onComplete();
        });
    }

    @Override
    public Completable saveUser(User user) {
        return Completable.create(emitter -> {
            Log.d(TAG, String.format("Save user. Id: %s", user.getID()));
            if (validateUser(user)) {
                saveUserData(
                        user.getAccessToken(),
                        user.getID(),
                        user.getUserName(),
                        user.getDisplayName(),
                        user.getUserType(),
                        user.getLoginType(),
                        user.getJarvisUserId(),
                        user.getJarvisUserName(),
                        user.getJarvisAccessToken(),
                        user.getJarvisRefreshToken());
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable("Invalid user"));
            }
        });
    }

    /**
     * Persists the user data in the device shared preference.
     * @param accessToken the access token of the user account.
     * @param id the user id of the account.
     * @param userName the user name representing the account.
     * @param displayName the display name of the account.
     * @param userType the user type of the account.
     * @param loginType the type of login user for the user.
     * @param jarvisUserId user id for Jarvis user.
     * @param jarvisUserName user name for Jarvis user.
     * @param jarvisAccessToken access token for Jarvis user.
     * @param jarvisRefreshToken refresh token for Jarvis user.
     */
    private void saveUserData(
            String accessToken,
            String id,
            String userName,
            String displayName,
            int userType,
            String loginType,
            int jarvisUserId,
            String jarvisUserName,
            String jarvisAccessToken,
            String jarvisRefreshToken
    ) {
        appPrefs.saveStringPreference(PreferenceTypes.K_ACCESS_TOKEN, accessToken);
        appPrefs.saveStringPreference(PreferenceTypes.K_USER_ID, id);
        appPrefs.saveStringPreference(PreferenceTypes.K_USER_NAME, userName);
        appPrefs.saveStringPreference(PreferenceTypes.K_DISPLAY_NAME, displayName);
        appPrefs.saveIntPreference(PreferenceTypes.K_USER_TYPE, userType);
        appPrefs.saveStringPreference(PreferenceTypes.K_LOGIN_TYPE, loginType);
        appPrefs.saveIntPreference(PreferenceTypes.JARVIS_USER_ID, jarvisUserId);
        appPrefs.saveStringPreference(PreferenceTypes.JARVIS_USER_NAME, jarvisUserName);
        appPrefs.saveStringPreference(PreferenceTypes.JARVIS_ACCESS_TOKEN, jarvisAccessToken);
        appPrefs.saveStringPreference(PreferenceTypes.JARVIS_REFRESH_TOKEN, jarvisRefreshToken);
    }

    /**
     * @param user the user which will be validated.
     * @return {@code true} if the user is valid, {@code false} otherwise.
     */
    private boolean validateUser(User user) {
        return user != null && validateUser(user.getUserName(), user.getAccessToken());
    }

    /**
     * @param userName the username which will be validated.
     * @param accessToken the accessToken which will be validated.
     * @return {@code true} if the user is valid, {@code false} otherwise.
     */
    private boolean validateUser(String userName, String accessToken) {
        return !StringUtils.isEmpty(userName)
                && !StringUtils.isEmpty(accessToken);
    }
}
