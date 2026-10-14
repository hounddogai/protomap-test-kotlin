package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelCheckpointEngine(newRegistry())
        val item = newParcelCheckpoint("code-306", "name-393")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
