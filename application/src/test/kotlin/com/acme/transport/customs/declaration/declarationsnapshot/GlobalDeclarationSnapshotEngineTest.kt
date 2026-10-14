package com.acme.transport.customs.declaration.declarationsnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDeclarationSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDeclarationSnapshotEngine()
        val item = newGlobalDeclarationSnapshot("code-832", "label-706")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
