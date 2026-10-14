package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDeclarationSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDeclarationSnapshotEngine(newRegistry())
        val item = newManualDeclarationSnapshot("code-171", "name-498")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
