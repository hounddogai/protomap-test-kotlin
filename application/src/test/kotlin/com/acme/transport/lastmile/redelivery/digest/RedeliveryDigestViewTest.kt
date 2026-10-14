package com.acme.transport.lastmile.redelivery.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryDigest("code-651", "title-995")
        val view = toRedeliveryDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryDigestViews(mutableListOf<RedeliveryDigest>(item, item)).size, "views")
    }
}
