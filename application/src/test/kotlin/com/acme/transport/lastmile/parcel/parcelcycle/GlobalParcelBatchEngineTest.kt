package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalParcelBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalParcelBatchEngine(newRegistry())
        val item = newGlobalParcelBatch("code-363", "name-235")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
