package com.acme.transport.network.cutoff.cutofftarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCutoffOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCutoffOverrideStore()
        val item = newRegionalCutoffOverride("code-828", "label-496")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
