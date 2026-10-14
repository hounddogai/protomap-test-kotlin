package com.acme.transport.safety.cameraevent.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventAgreement("code-335", "label-505")
        val view = toCameraEventAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventAgreementViews(mutableListOf<CameraEventAgreement>(item, item)).size, "views")
    }
}
