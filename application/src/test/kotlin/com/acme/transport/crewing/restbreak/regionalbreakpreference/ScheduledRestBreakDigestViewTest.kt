package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledRestBreakDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledRestBreakDigest("code-199", "title-306")
        val view = toScheduledRestBreakDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledRestBreakDigestViews(mutableListOf<ScheduledRestBreakDigest>(item, item)).size, "views")
    }
}
