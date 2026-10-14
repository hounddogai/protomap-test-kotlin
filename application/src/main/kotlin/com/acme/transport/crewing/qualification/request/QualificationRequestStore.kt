package com.acme.transport.crewing.qualification.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification requests by code. */
interface QualificationRequestStore {
    fun find(code: String): QualificationRequest?
    fun save(item: QualificationRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationRequestStore(
    var items: MutableMap<String, QualificationRequest>,
    var order: MutableList<String>,
) : QualificationRequestStore {
    override fun find(code: String): QualificationRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationRequest) {
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

    override fun list(): MutableList<QualificationRequest> {
        val out = mutableListOf<QualificationRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationRequestStore(): QualificationRequestStore {
    return MemoryQualificationRequestStore(
        mutableMapOf<String, QualificationRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationRequestStore(
    var inner: QualificationRequestStore,
    var cache: TtlCache<QualificationRequest>,
    var hits: Int,
    var misses: Int,
) : QualificationRequestStore {
    override fun find(code: String): QualificationRequest? {
        val hit = (cache.get(code) as QualificationRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationRequest> = inner.list()
}

fun newCachedQualificationRequestStore(inner: QualificationRequestStore, ttl: Int, clock: Clock): QualificationRequestStore {
    return CachedQualificationRequestStore(
        inner = inner,
        cache = newTtlCache<QualificationRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification requests. Empty fields match all. */
data class QualificationRequestQuery(
    var locale: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationRequest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationRequest>): MutableList<QualificationRequest> {
        val matched = filter(items) { candidate: QualificationRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationRequests(store: QualificationRequestStore, query: QualificationRequestQuery): MutableList<QualificationRequest> {
    return query.apply(store.list())
}
