package com.acme.transport.safety.coaching.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingDigest("code-820", "title-170")
        val view = toCoachingDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingDigestViews(mutableListOf<CoachingDigest>(item, item)).size, "views")
    }
}
