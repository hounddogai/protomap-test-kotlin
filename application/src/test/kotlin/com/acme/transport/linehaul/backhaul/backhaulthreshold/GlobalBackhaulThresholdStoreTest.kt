package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalBackhaulThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalBackhaulThresholdStore()
        val item = newGlobalBackhaulThreshold("code-93", "name-89")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
