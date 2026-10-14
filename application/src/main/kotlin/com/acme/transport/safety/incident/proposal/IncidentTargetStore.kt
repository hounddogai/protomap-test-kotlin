package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident targets by code. */
interface IncidentTargetStore {
    fun find(code: String): IncidentTarget?
    fun save(item: IncidentTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentTargetStore(
    var items: MutableMap<String, IncidentTarget>,
    var order: MutableList<String>,
) : IncidentTargetStore {
    override fun find(code: String): IncidentTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentTarget) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<IncidentTarget> {
        val out = mutableListOf<IncidentTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentTargetStore(): IncidentTargetStore {
    return MemoryIncidentTargetStore(mutableMapOf<String, IncidentTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentTargetStore(
    var inner: IncidentTargetStore,
    var cache: TtlCache<IncidentTarget>,
    var hits: Int,
    var misses: Int,
) : IncidentTargetStore {
    override fun find(code: String): IncidentTarget? {
        val hit = (cache.get(code) as IncidentTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentTarget> = inner.list()
}

fun newCachedIncidentTargetStore(inner: IncidentTargetStore, ttl: Int, clock: Clock): IncidentTargetStore {
    return CachedIncidentTargetStore(
        inner = inner,
        cache = newTtlCache<IncidentTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident targets. Empty fields match all. */
data class IncidentTargetQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentTarget>): MutableList<IncidentTarget> {
        val matched = filter(items) { candidate: IncidentTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentTargets(store: IncidentTargetStore, query: IncidentTargetQuery): MutableList<IncidentTarget> {
    return query.apply(store.list())
}
