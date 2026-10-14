package com.acme.transport.lastmile.redelivery.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRedeliveryDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRedeliveryDigestStore()
        val item = newScheduledRedeliveryDigest("code-978", "label-817")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
