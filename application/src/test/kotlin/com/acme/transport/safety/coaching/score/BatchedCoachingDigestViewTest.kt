package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCoachingDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCoachingDigest("code-280", "name-628")
        val view = toBatchedCoachingDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCoachingDigestViews(mutableListOf<BatchedCoachingDigest>(item, item)).size, "views")
    }
}
