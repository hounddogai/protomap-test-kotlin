package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldPreferenceStore()
        val item = newHoldPreference("code-786", "name-443")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
