package com.acme.transport.terminal.crossdock.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredCrossDockClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredCrossDockClaim("code-399", "name-952")
        val view = toTieredCrossDockClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredCrossDockClaimViews(mutableListOf<TieredCrossDockClaim>(item, item)).size, "views")
    }
}
