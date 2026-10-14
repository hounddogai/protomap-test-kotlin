package com.acme.transport.safety.coaching.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedCoachingDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedCoachingDigest("code-444", "name-189")
        val view = toCappedCoachingDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedCoachingDigestViews(mutableListOf<CappedCoachingDigest>(item, item)).size, "views")
    }
}
