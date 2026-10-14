package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy inspection requests by code. */
interface LegacyInspectionRequestStore {
    fun find(code: String): LegacyInspectionRequest?
    fun save(item: LegacyInspectionRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyInspectionRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyInspectionRequestStore(
    var items: MutableMap<String, LegacyInspectionRequest>,
    var order: MutableList<String>,
) : LegacyInspectionRequestStore {
    override fun find(code: String): LegacyInspectionRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyInspectionRequest) {
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

    override fun list(): MutableList<LegacyInspectionRequest> {
        val out = mutableListOf<LegacyInspectionRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyInspectionRequestStore(): LegacyInspectionRequestStore {
    return MemoryLegacyInspectionRequestStore(
        mutableMapOf<String, LegacyInspectionRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyInspectionRequestStore(
    var inner: LegacyInspectionRequestStore,
    var cache: TtlCache<LegacyInspectionRequest>,
    var hits: Int,
    var misses: Int,
) : LegacyInspectionRequestStore {
    override fun find(code: String): LegacyInspectionRequest? {
        val hit = (cache.get(code) as LegacyInspectionRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyInspectionRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyInspectionRequest> = inner.list()
}

fun newCachedLegacyInspectionRequestStore(inner: LegacyInspectionRequestStore, ttl: Int, clock: Clock): LegacyInspectionRequestStore {
    return CachedLegacyInspectionRequestStore(
        inner = inner,
        cache = newTtlCache<LegacyInspectionRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy inspection requests. Empty fields match all. */
data class LegacyInspectionRequestQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyInspectionRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyInspectionRequest>): MutableList<LegacyInspectionRequest> {
        val matched = filter(items) { candidate: LegacyInspectionRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyInspectionRequests(store: LegacyInspectionRequestStore, query: LegacyInspectionRequestQuery): MutableList<LegacyInspectionRequest> {
    return query.apply(store.list())
}
