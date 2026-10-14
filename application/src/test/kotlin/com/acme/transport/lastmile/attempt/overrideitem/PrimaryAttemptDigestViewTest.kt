package com.acme.transport.lastmile.attempt.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryAttemptDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryAttemptDigest("code-661", "label-173")
        val view = toPrimaryAttemptDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryAttemptDigestViews(mutableListOf<PrimaryAttemptDigest>(item, item)).size, "views")
    }
}
