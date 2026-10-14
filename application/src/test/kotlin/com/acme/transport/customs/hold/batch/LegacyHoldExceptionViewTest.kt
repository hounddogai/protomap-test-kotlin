package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyHoldExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyHoldException("code-429", "name-14")
        val view = toLegacyHoldExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyHoldExceptionViews(mutableListOf<LegacyHoldException>(item, item)).size, "views")
    }
}
