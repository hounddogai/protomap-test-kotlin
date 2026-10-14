package com.acme.transport.network.servicearea.areatarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveServiceAreaTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveServiceAreaTargetStore()
        val item = newAdaptiveServiceAreaTarget("code-741", "label-736")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
