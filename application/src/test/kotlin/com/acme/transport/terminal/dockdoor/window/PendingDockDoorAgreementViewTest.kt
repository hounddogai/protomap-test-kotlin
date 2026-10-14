package com.acme.transport.terminal.dockdoor.window

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDockDoorAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDockDoorAgreement("code-68", "label-518")
        val view = toPendingDockDoorAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDockDoorAgreementViews(mutableListOf<PendingDockDoorAgreement>(item, item)).size, "views")
    }
}
