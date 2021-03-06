import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Redirect } from 'react-router';
import { Link } from 'react-router-dom';
import { parse } from 'qs';
import moment from 'moment';
import { userIsNotLoggedIn } from '../common/login';
import {
    LOGOUT_REQUEST,
} from '../actiontypes';

function PerformedPayments(props) {
    if (userIsNotLoggedIn(props)) {
        return <Redirect to="/ukelonn/login" />;
    }

    let { account, payments, onLogout } = props;
    let queryParams = parse(props.location.search, { ignoreQueryPrefix: true });
    const { parentTitle } = queryParams;

    return (
        <div>
            <Link className="btn btn-block btn-primary mb-0 left-align-cell" to="/ukelonn/">
                <span className="oi oi-chevron-left" title="chevron left" aria-hidden="true"></span>
                &nbsp;
                {parentTitle}
            </Link>
            <header>
                <div className="pb-2 mt-0 mb-2 border-bottom bg-light">
                    <h1>Utbetalinger til {account.firstName}</h1>
                </div>
            </header>
            <div className="table-responsive table-sm table-striped">
                <table className="table">
                    <thead>
                        <tr>
                            <th className="transaction-table-col transaction-table-col1">Dato</th>
                            <th className="transaction-table-col transaction-table-col-hide-overflow transaction-table-col2">Utbetalinger</th>
                            <th className="transaction-table-col transaction-table-col3b">Beløp</th>
                        </tr>
                    </thead>
                    <tbody>
                        {payments.map((payment) =>
                             <tr key={payment.id}>
                                 <td className="transaction-table-col">{moment(payment.transactionTime).format("YYYY-MM-DD")}</td>
                                 <td className="transaction-table-col">{payment.name}</td>
                                 <td className="transaction-table-col">{payment.transactionAmount}</td>
                             </tr>
                        )}
                    </tbody>
                </table>
            </div>
            <button className="btn btn-default" onClick={() => onLogout()}>Logout</button>
            <br/>
            <a href="../..">Tilbake til topp</a>
        </div>
    );
}

function mapStateToProps(state) {
    return {
        haveReceivedResponseFromLogin: state.haveReceivedResponseFromLogin,
        loginResponse: state.loginResponse,
        parentTitle: state.parentTitle,
        account: state.account,
        payments: state.payments,
    };
}

function mapDispatchToProps(dispatch) {
    return {
        onLogout: () => dispatch(LOGOUT_REQUEST()),
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(PerformedPayments);
