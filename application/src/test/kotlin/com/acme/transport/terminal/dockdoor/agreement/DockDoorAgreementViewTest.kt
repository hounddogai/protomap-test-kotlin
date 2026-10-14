package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorAgreement("code-398", "name-983")
        val view = toDockDoorAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorAgreementViews(mutableListOf<DockDoorAgreement>(item, item)).size, "views")
    }
}
