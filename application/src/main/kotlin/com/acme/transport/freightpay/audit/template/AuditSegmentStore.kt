package com.acme.transport.freightpay.audit.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit segments by code. */
interface AuditSegmentStore {
    fun find(code: String): AuditSegment?
    fun save(item: AuditSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditSegmentStore(
    var items: MutableMap<String, AuditSegment>,
    var order: MutableList<String>,
) : AuditSegmentStore {
    override fun find(code: String): AuditSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditSegment) {
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

    override fun list(): MutableList<AuditSegment> {
        val out = mutableListOf<AuditSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditSegmentStore(): AuditSegmentStore {
    return MemoryAuditSegmentStore(mutableMapOf<String, AuditSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditSegmentStore(
    var inner: AuditSegmentStore,
    var cache: TtlCache<AuditSegment>,
    var hits: Int,
    var misses: Int,
) : AuditSegmentStore {
    override fun find(code: String): AuditSegment? {
        val hit = (cache.get(code) as AuditSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditSegment> = inner.list()
}

fun newCachedAuditSegmentStore(inner: AuditSegmentStore, ttl: Int, clock: Clock): AuditSegmentStore {
    return CachedAuditSegmentStore(
        inner = inner,
        cache = newTtlCache<AuditSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit segments. Empty fields match all. */
data class AuditSegmentQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditSegment>): MutableList<AuditSegment> {
        val matched = filter(items) { candidate: AuditSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditSegments(store: AuditSegmentStore, query: AuditSegmentQuery): MutableList<AuditSegment> {
    return query.apply(store.list())
}
