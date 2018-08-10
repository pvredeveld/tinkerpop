﻿#region License

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#endregion

using System;
using System.Collections.Generic;

namespace Gremlin.Net.Driver.Exceptions
{
    /// <summary>
    ///     The exception that is thrown when a response is received from Gremlin Server that indicates that an error occurred.
    /// </summary>
    public class ResponseException : Exception
    {
        /// <summary>
        ///     Initializes a new instance of the <see cref="ResponseException" /> class.
        /// </summary>
        /// <param name="message">The error message string.</param>
        public ResponseException(string message) : base(message)
        {
        }

        /// <summary>
        ///     Initializes a new instance of the <see cref="ResponseException" /> class.
        /// </summary>
        /// <param name="message">The error message string.</param>
        /// <param name="attributes">The attributes regarding the error provided by the driver.</param>
        public ResponseException(string message, IDictionary<string,object> attributes) : base(message)
        {
            Attributes = attributes;
        }

        /// <summary>
        ///     The attributes regarding the error provided by the driver.
        /// </summary>
        public IDictionary<string, object> Attributes { get; }
    }
}