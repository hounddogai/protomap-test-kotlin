package com.acme.transport.safety.cameraevent.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newCameraEventAgreement("code-335", "label-505")
        assertEquals(CameraEventAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newCameraEventAgreement("code-335", "label-505")
        try {
            item.moveTo(CameraEventAgreementStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventAgreementStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCameraEventAgreement("code-335", "label-505")
        item.status = CameraEventAgreementStatus.FAILED
        assertFails("move") { item.moveTo(CameraEventAgreementStatus.NEW) }
    }
}
