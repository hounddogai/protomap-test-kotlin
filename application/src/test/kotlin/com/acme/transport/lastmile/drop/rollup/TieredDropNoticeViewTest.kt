package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredDropNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredDropNotice("code-377", "title-804")
        val view = toTieredDropNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredDropNoticeViews(mutableListOf<TieredDropNotice>(item, item)).size, "views")
    }
}
