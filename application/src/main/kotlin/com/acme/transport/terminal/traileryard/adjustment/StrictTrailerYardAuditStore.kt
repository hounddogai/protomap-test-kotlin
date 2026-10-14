package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict trailer yard audits by code. */
interface StrictTrailerYardAuditStore {
    fun find(code: String): StrictTrailerYardAudit?
    fun save(item: StrictTrailerYardAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTrailerYardAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTrailerYardAuditStore(
    var items: MutableMap<String, StrictTrailerYardAudit>,
    var order: MutableList<String>,
) : StrictTrailerYardAuditStore {
    override fun find(code: String): StrictTrailerYardAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTrailerYardAudit) {
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

    override fun list(): MutableList<StrictTrailerYardAudit> {
        val out = mutableListOf<StrictTrailerYardAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTrailerYardAuditStore(): StrictTrailerYardAuditStore {
    return MemoryStrictTrailerYardAuditStore(
        mutableMapOf<String, StrictTrailerYardAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTrailerYardAuditStore(
    var inner: StrictTrailerYardAuditStore,
    var cache: TtlCache<StrictTrailerYardAudit>,
    var hits: Int,
    var misses: Int,
) : StrictTrailerYardAuditStore {
    override fun find(code: String): StrictTrailerYardAudit? {
        val hit = (cache.get(code) as StrictTrailerYardAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTrailerYardAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTrailerYardAudit> = inner.list()
}

fun newCachedStrictTrailerYardAuditStore(inner: StrictTrailerYardAuditStore, ttl: Int, clock: Clock): StrictTrailerYardAuditStore {
    return CachedStrictTrailerYardAuditStore(
        inner = inner,
        cache = newTtlCache<StrictTrailerYardAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict trailer yard audits. Empty fields match all. */
data class StrictTrailerYardAuditQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTrailerYardAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTrailerYardAudit>): MutableList<StrictTrailerYardAudit> {
        val matched = filter(items) { candidate: StrictTrailerYardAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTrailerYardAudits(store: StrictTrailerYardAuditStore, query: StrictTrailerYardAuditQuery): MutableList<StrictTrailerYardAudit> {
    return query.apply(store.list())
}
