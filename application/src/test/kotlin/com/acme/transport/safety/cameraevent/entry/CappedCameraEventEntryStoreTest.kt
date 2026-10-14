package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedCameraEventEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedCameraEventEntryStore()
        val item = newCappedCameraEventEntry("code-448", "name-852")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
