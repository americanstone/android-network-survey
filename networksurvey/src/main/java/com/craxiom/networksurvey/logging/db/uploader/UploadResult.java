/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.craxiom.networksurvey.logging.db.uploader;

/**
 * This code was pulled from the Tower Collector app and modified to work with Network Survey.
 * <p>
 * See: <a href="https://github.com/zamojski/TowerCollector/blob/e7709a4a74a113bf9cccc8db0ecd0cd04b022383/app/src/main/java/info/zamojski/soft/towercollector/enums/UploadResult.java#L7">here</a>
 */
public enum UploadResult
{
    NotStarted, NoData, Success, PartiallySucceeded, ConnectionError, ServerError, InvalidApiKey, InvalidData, Failure, DeleteFailed, Cancelled, PermissionDenied, LimitExceeded
}
