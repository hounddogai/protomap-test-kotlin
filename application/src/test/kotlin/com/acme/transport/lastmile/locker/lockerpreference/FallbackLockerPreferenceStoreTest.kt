package com.acme.transport.lastmile.locker.lockerpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackLockerPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackLockerPreferenceStore()
        val item = newFallbackLockerPreference("code-229", "label-48")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
