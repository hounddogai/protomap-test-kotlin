package com.acme.transport.fleetops.tire.tireagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTireBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTireBundleStore()
        val item = newScheduledTireBundle("code-118", "label-564")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
